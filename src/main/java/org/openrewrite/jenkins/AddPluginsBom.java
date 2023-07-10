/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.jenkins;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.maven.AddManagedDependency;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.RemoveRedundantDependencyVersions;
import org.openrewrite.maven.tree.ResolvedDependency;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddPluginsBom extends ScanningRecipe<AddPluginsBom.Scanned> {
    private static final BomLookup LOOKUP = new BomLookup();
    public static final String PLUGINS_BOM_GROUP_ID = "io.jenkins.tools.bom";

    @Override
    public String getDisplayName() {
        return "Add or correct Jenkins plugins BOM";
    }

    @Override
    public String getDescription() {
        return "Adds [Jenkins plugins BOM](https://www.jenkins.io/doc/developer/plugin-development/dependency-management/#jenkins-plugin-bom) " +
                "at the latest release if the project depends on any managed versions. " +
                "BOMs are expected to be synchronized to Jenkins LTS versions, so this will also remove any mismatched BOMs (Such as using Jenkins 2.387.3, but importing bom-2.319.x). " +
                "If the expected BOM is already added, the version will not be upgraded.";
    }

    @Override
    public Scanned getInitialValue(ExecutionContext ctx) {
        return new Scanned();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Scanned acc) {
        return new MavenIsoVisitor<ExecutionContext>() {
            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext executionContext) {
                Xml.Document d = super.visitDocument(document, executionContext);
                if (acc.needsPluginsBom()) {
                    return SearchResult.found(d);
                }
                return d;
            }

            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                Xml.Tag t = super.visitTag(tag, executionContext);
                if (isPropertyTag()) {
                    if (Objects.equals("jenkins.version", t.getName())) {
                        acc.jenkinsVersion = t.getValue().orElse("");
                    }
                } else if (isManagedDependencyTag()) {
                    String groupId = tag.getChildValue("groupId").orElse("");
                    String artifactId = tag.getChildValue("artifactId").orElse("");
                    if (PLUGINS_BOM_GROUP_ID.equals(groupId) && !artifactId.isEmpty()) {
                        acc.foundPluginsBoms.add(new Artifact(groupId, artifactId));
                    }
                } else {
                    ResolvedDependency dependency = findDependency(tag);
                    if (dependency != null && LOOKUP.inBom(dependency.getGroupId(), dependency.getArtifactId())) {
                        acc.foundPlugins.add(new Artifact(dependency.getGroupId(), dependency.getArtifactId()));
                    }
                }
                return t;
            }
        };
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Scanned acc) {
        return Preconditions.check(acc.needsPluginsBom(), new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
                Xml maven = super.visitDocument(document, executionContext);
                doAfterVisit(new AddManagedDependency(
                        "io.jenkins.tools.bom",
                        acc.bomName(),
                        "latest.release",
                        "import",
                        "pom",
                        null,
                        null,
                        true,
                        null,
                        null
                ).getVisitor());
                for (Artifact artifact : acc.foundPlugins) {
                    doAfterVisit(new RemoveRedundantDependencyVersions(
                            artifact.getGroupId(),
                            artifact.getArtifactId(),
                            false,
                            null
                    ).getVisitor());
                }
                return maven;
            }

            /**
             * Modeled after {@link org.openrewrite.maven.RemoveManagedDependency}, which does not allow removing
             * imported BOMs.
             */
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                Xml.Tag t = (Xml.Tag) super.visitTag(tag, executionContext);
                if (isManagedDependencyTag()) {
                    boolean isImport = "import".equals(t.getChildValue("scope").orElse(""));
                    boolean isPom = "pom".equals(t.getChildValue("type").orElse(""));
                    String groupId = t.getChildValue("groupId").orElse("");
                    String artifactId = t.getChildValue("artifactId").orElse("");
                    Artifact artifact = new Artifact(groupId, artifactId);
                    if (isPom && isImport && acc.bomsToRemove().contains(artifact)) {
                        doAfterVisit(new RemoveContentVisitor<>(tag, true));
                    }
                }
                return t;
            }
        });
    }
    
    @Value
    static class Artifact {
        String groupId, artifactId;
    }
    
    static class Scanned {
        private static final Predicate<String> LTS_PATTERN = Pattern.compile("^\\d\\.\\d+\\.\\d$").asPredicate();
        final Set<Artifact> foundPlugins = new HashSet<>();
        final Set<Artifact> foundPluginsBoms = new HashSet<>();
        String jenkinsVersion = "";
        
        boolean needsPluginsBom() {
            boolean hasPluginsThatBomIncludes = !foundPlugins.isEmpty();
            boolean hasJenkinsVersion = !jenkinsVersion.isEmpty();
            Set<Artifact> expected = new HashSet<>();
            expected.add(new Artifact("io.jenkins.tools.bom", bomName()));
            boolean hasOnlyExpectedPluginsBom = foundPluginsBoms.equals(expected);
            return hasPluginsThatBomIncludes && hasJenkinsVersion && !hasOnlyExpectedPluginsBom;
        }
        
        String bomName() {
            boolean isLts = LTS_PATTERN.test(jenkinsVersion);
            if (!isLts) {
                return "bom-weekly";
            }
            int lastIndex = jenkinsVersion.lastIndexOf(".");
            String prefix = jenkinsVersion.substring(0, lastIndex);
            return "bom-" + prefix + ".x";
        }
        
        Set<Artifact> bomsToRemove() {
            Set<Artifact> remove = new HashSet<>();
            for (Artifact bom : foundPluginsBoms) {
                if (!Objects.equals(bomName(), bom.getArtifactId())) {
                    remove.add(bom);
                }
            }
            return remove;
        }
    }
}
