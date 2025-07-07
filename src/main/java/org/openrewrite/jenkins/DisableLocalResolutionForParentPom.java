/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
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
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.AddOrUpdateChild;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

/**
 * Disables local file resolution for parent POM, as recommended by the
 * <a href="https://www.jenkins.io/doc/developer/plugin-development/updating-parent/">plugin development guide</a>.
 */
@EqualsAndHashCode(callSuper = false)
@Value
public class DisableLocalResolutionForParentPom extends Recipe {
    @Override
    public String getDisplayName() {
        return "Disables local file resolution for parent POM";
    }

    @Override
    public String getDescription() {
        return "Explicitly sets `<relativePath/>` to disable file resolution, as recommended in the " +
                "[plugin development guide](https://www.jenkins.io/doc/developer/plugin-development/updating-parent/).";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext ctx) {
                if (isParentTag()) {
                    Tag relativePathTag = Tag.build("<relativePath />");
                    return AddOrUpdateChild.addOrUpdateChild(tag, relativePathTag, getCursor().getParentOrThrow());
                }
                return super.visitTag(tag, ctx);
            }
        };
    }
}
