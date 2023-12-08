/*
 * Copyright 2021 the original author or authors.
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
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.AddDependencyVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.RemoveDependency;
import org.openrewrite.maven.tree.ResolvedDependency;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Replaces a set of libraries with an api plugin.
 * Excludes libraries transitively with comments.
 * Jenkins has as custom classloader that shares libraries through api plugins.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class ReplaceLibrariesWithApiPlugin extends Recipe {
    private static final XPathMatcher DEPENDENCIES_MATCHER = new XPathMatcher("/project/dependencies");

    @Option(displayName = "API Plugin's groupId",
            description = "The first part of a dependency coordinate 'io.jenkins.plugins:ARTIFACT_ID:VERSION'.",
            example = "io.jenkins.plugins")
    String pluginGroupId;

    @Option(displayName = "API Plugin's artifactId",
            description = "The second part of a dependency coordinate 'GROUP_ID:jackson2-api:VERSION'.",
            example = "jackson2-api")
    String pluginArtifactId;

    @Option(displayName = "API Plugin's version",
            description = "An exact version number.",
            example = "1981.v17df70e84a_a_1")
    String pluginVersion;

    @Option(displayName = "Replaced Libraries",
            description = "The set of library coordinates replaced by this API Plugin.")
    Set<Library> replaces;

    /**
     * The groupId:artifactId combos to be replaced if present.
     */
    @Value
    public static class Library {
        String groupId;
        String artifactId;
    }

    /**
     * Replaces a set of libraries with an api plugin.
     * @param pluginGroupId api plugin's groupId
     * @param pluginArtifactId api plugin's artifactId
     * @param pluginVersion api plugin's version
     * @param replaces set of libraries included in the api plugin
     */
    public ReplaceLibrariesWithApiPlugin(
            String pluginGroupId,
            String pluginArtifactId,
            String pluginVersion,
            Set<Library> replaces) {
        this.pluginGroupId = pluginGroupId;
        this.pluginArtifactId = pluginArtifactId;
        this.pluginVersion = pluginVersion;
        this.replaces = replaces;
    }

    @Override
    public String getDisplayName() {
        return "Use Jenkins API plugin instead of libraries";
    }

    @Override
    public String getDescription() {
        return "Prefer Jenkins API plugins over bundling libraries for slimmer plugins.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext ctx) {
                if (isDependencyTag()) {
                    ResolvedDependency dependency = findDependency(tag);
                    if (dependency != null && !isApiPlugin(dependency)) {
                        for (Library replaced : replaces) {
                            String groupId = replaced.groupId;
                            String artifactId = replaced.artifactId;
                            ResolvedDependency found = dependency.findDependency(groupId, artifactId);
                            if (found == null) {
                                continue;
                            }
                            doAfterVisit(new AddDependencyVisitor(
                                    pluginGroupId,
                                    pluginArtifactId,
                                    pluginVersion,
                                    null,
                                    null,
                                    true,
                                    null,
                                    null,
                                    false,
                                    null
                            ));
                            doAfterVisit(new RemoveDependency(groupId, artifactId, null).getVisitor());
                            if (found != dependency) {
                                Optional<Tag> maybeExclusions = tag.getChild("exclusions");
                                if (maybeExclusions.isPresent()) {
                                    Tag exclusions = maybeExclusions.get();

                                    List<Tag> individualExclusions = exclusions.getChildren("exclusion");
                                    if (individualExclusions.stream().noneMatch(exclusion ->
                                            groupId.equals(exclusion.getChildValue("groupId").orElse(null)) &&
                                                    artifactId.equals(exclusion.getChildValue("artifactId").orElse(null)))) {
                                        doAfterVisit(new AddToTagVisitor<>(exclusions, Tag.build("" +
                                                "<exclusion>\n" +
                                                "<!-- brought in by " + pluginGroupId + ":" + pluginArtifactId + " -->\n" +
                                                "<groupId>" + groupId + "</groupId>\n" +
                                                "<artifactId>" + artifactId + "</artifactId>\n" +
                                                "</exclusion>")));
                                    }
                                } else {
                                    doAfterVisit(new AddToTagVisitor<>(tag, Tag.build("" +
                                            "<exclusions>\n" +
                                            "<exclusion>\n" +
                                            "<!-- brought in by " + pluginGroupId + ":" + pluginArtifactId + " -->\n" +
                                            "<groupId>" + groupId + "</groupId>\n" +
                                            "<artifactId>" + artifactId + "</artifactId>\n" +
                                            "</exclusion>\n" +
                                            "</exclusions>")));
                                }
                                maybeUpdateModel();
                            }
                        }
                    }
                }
                return super.visitTag(tag, ctx);
            }

            private boolean isApiPlugin(ResolvedDependency dependency) {
                return pluginGroupId.equals(dependency.getGroupId()) && pluginArtifactId.equals(dependency.getArtifactId());
            }
        };
    }
}
