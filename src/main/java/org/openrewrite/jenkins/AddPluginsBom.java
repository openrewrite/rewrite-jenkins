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
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;
import org.openrewrite.maven.AddManagedDependency;
import org.openrewrite.maven.ChangeManagedDependencyGroupIdAndArtifactId;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.maven.RemoveRedundantDependencyVersions;
import org.openrewrite.maven.tree.*;
import org.openrewrite.xml.RemoveContentVisitor;
import org.openrewrite.xml.tree.Xml;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Value
@EqualsAndHashCode(callSuper = false)
public class AddPluginsBom extends Recipe {
    private static final BomLookup LOOKUP = new BomLookup();
    private static final String PLUGINS_BOM_GROUP_ID = "io.jenkins.tools.bom";
    private static final String LATEST_RELEASE = "latest.release";
    private static final String VERSION_METADATA_PATTERN = "\\.v[a-f0-9_]+";

    @Override
    public String getDisplayName() {
        return "Add or correct Jenkins plugins BOM";
    }

    @Override
    public String getDescription() {
        return "Adds [Jenkins plugins BOM](https://www.jenkins.io/doc/developer/plugin-development/dependency-management/#jenkins-plugin-bom) " +
                "at the latest release if the project depends on any managed versions or an outdated BOM is present. " +
                "BOMs are expected to be synchronized to Jenkins LTS versions, so this will also remove any mismatched BOMs (Such as using Jenkins 2.387.3, but importing bom-2.319.x). " +
                "If the expected BOM is already added, the version will not be upgraded.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenIsoVisitor<ExecutionContext>() {
            private boolean alreadyChanged = false;
            private String bomName = "";

            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext ctx) {
                Xml.Document d = super.visitDocument(document, ctx);
                Markers m = document.getMarkers();
                Optional<MavenResolutionResult> maybeMavenResult = m.findFirst(MavenResolutionResult.class);
                if (!maybeMavenResult.isPresent()) {
                    return d;
                }
                MavenResolutionResult result = maybeMavenResult.get();
                ResolvedPom resolvedPom = result.getPom();
                Pom pom = resolvedPom.getRequested();
                List<ManagedDependency> dependencyManagement = pom.getDependencyManagement();
                boolean bomFound = false;
                for (ManagedDependency md : dependencyManagement) {
                    if (PLUGINS_BOM_GROUP_ID.equals(md.getGroupId())) {
                        bomFound = true;
                        break;
                    }
                }
                boolean hasDependencyInBom = false;
                List<Dependency> dependencies = pom.getDependencies();
                for (Dependency dependency : dependencies) {
                    String groupId = dependency.getGroupId();
                    String version = dependency.getVersion();
                    if (groupId == null || version == null) {
                        continue;
                    }
                    if (!hasDependencyInBom && LOOKUP.inBom(groupId, dependency.getArtifactId())) {
                        hasDependencyInBom = true;
                    }
                }
                if (!bomFound && hasDependencyInBom) {
                    return (Xml.Document) new AddManagedDependency(
                            PLUGINS_BOM_GROUP_ID,
                            bomName,
                            LATEST_RELEASE,
                            "import",
                            "pom",
                            null,
                            VERSION_METADATA_PATTERN,
                            true,
                            null,
                            null
                    ).getVisitor().visitNonNull(d, ctx, getCursor().getParentOrThrow());
                }
                return document;
            }

            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
                Xml.Tag t = super.visitTag(tag, ctx);
                if (isManagedDependencyTag()) {
                    String groupId = tag.getChildValue("groupId").orElse("");
                    String artifactId = tag.getChildValue("artifactId").orElse("");
                    if (PLUGINS_BOM_GROUP_ID.equals(groupId) && !artifactId.isEmpty()) {
                        if (alreadyChanged) {
                            doAfterVisit(new RemoveContentVisitor<>(t, true));
                        } else {
                            alreadyChanged = true;
                            if (!Objects.equals(bomName, artifactId)) {
                                doAfterVisit(new ChangeManagedDependencyGroupIdAndArtifactId(
                                        groupId,
                                        artifactId,
                                        groupId,
                                        bomName,
                                        LATEST_RELEASE,
                                        VERSION_METADATA_PATTERN
                                ).getVisitor());
                            }
                        }
                    }
                } else if (isPropertyTag() && Objects.equals("jenkins.version", t.getName())) {
                    String jenkinsVersion = t.getValue().orElseThrow(() ->
                            new IllegalStateException("No value found for jenkins.version property tag"));
                    bomName = Jenkins.bomNameForJenkinsVersion(jenkinsVersion);
                }
                return t;
            }
        };
    }

    @Value
    static class Artifact {
        String groupId;
        String artifactId;
    }
}
