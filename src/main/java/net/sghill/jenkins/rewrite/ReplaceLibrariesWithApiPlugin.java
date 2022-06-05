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
package net.sghill.jenkins.rewrite;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.RemoveDependency;
import org.openrewrite.maven.tree.ResolvedDependency;
import org.openrewrite.xml.AddCommentToXmlTag;
import org.openrewrite.xml.AddToTagVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Value
@EqualsAndHashCode(callSuper = true)
public class ReplaceLibrariesWithApiPlugin extends Recipe {
    private static final XPathMatcher DEPENDENCIES_MATCHER = new XPathMatcher("/project/dependencies");
    String pluginGroupId;
    String pluginArtifactId;
    String pluginVersion;
    Set<Library> replaces;

    @Value
    public static class Library {
        String groupId;
        String artifactId;

        @JsonCreator
        public Library(@JsonProperty("groupId") String groupId, @JsonProperty("artifactId") String artifactId) {
            this.groupId = groupId;
            this.artifactId = artifactId;
        }
    }

    @JsonCreator
    public ReplaceLibrariesWithApiPlugin(
            @JsonProperty("pluginGroupId") String pluginGroupId,
            @JsonProperty("pluginArtifactId") String pluginArtifactId,
            @JsonProperty("pluginVersion") String pluginVersion,
            @JsonProperty("replaces") Set<Library> replaces) {
        this.pluginGroupId = pluginGroupId;
        this.pluginArtifactId = pluginArtifactId;
        this.pluginVersion = pluginVersion;
        this.replaces = replaces;
        for (Library replace : replaces) {
            String xPath = "/project/dependencies/dependency/exclusions/exclusion[./artifactId = '" + replace.artifactId + "']";
            doNext(new AddCommentToXmlTag(xPath, " brought in by " + pluginArtifactId + " "));
        }
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
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext executionContext) {
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
                            doNext(new AddDependency(pluginGroupId, pluginArtifactId, pluginVersion));
                            doNext(new RemoveDependency(groupId, artifactId, null));
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
                                                "<groupId>" + groupId + "</groupId>\n" +
                                                "<artifactId>" + artifactId + "</artifactId>\n" +
                                                "</exclusion>")));
                                    }
                                } else {
                                    doAfterVisit(new AddToTagVisitor<>(tag, Tag.build("" +
                                            "<exclusions>\n" +
                                            "<exclusion>\n" +
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
                return super.visitTag(tag, executionContext);
            }

            private boolean isApiPlugin(ResolvedDependency dependency) {
                return pluginGroupId.equals(dependency.getGroupId()) && pluginArtifactId.equals(dependency.getArtifactId());
            }
        };
    }
}
