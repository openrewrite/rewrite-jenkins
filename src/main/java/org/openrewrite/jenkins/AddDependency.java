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
import org.openrewrite.maven.AddDependencyVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

import java.util.List;

/**
 * Always adds a dependency to the plugin's POM
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class AddDependency extends Recipe {
    private static final XPathMatcher DEPENDENCIES_MATCHER = new XPathMatcher("/project/dependencies");

    String groupId;
    String artifactId;
    String version;

    @Override
    public String getDisplayName() {
        return "Adds a dependency unconditionally";
    }

    @Override
    public String getDescription() {
        return getDisplayName();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext executionContext) {
                if (DEPENDENCIES_MATCHER.matches(getCursor())) {
                    List<Tag> dependencies = tag.getChildren("dependency");
                    boolean missing = dependencies.stream().noneMatch(d -> groupId.equals(d.getChildValue("groupId").orElse(null)) &&
                            artifactId.equals(d.getChildValue("artifactId").orElse(null)));
                    if (dependencies.isEmpty() || missing) {
                        doAfterVisit(new AddDependencyVisitor(
                                groupId,
                                artifactId,
                                version,
                                null,
                                null,
                                true,
                                null,
                                null,
                                false,
                                null
                        ));
                    }
                }
                return super.visitTag(tag, executionContext);
            }
        };
    }
}
