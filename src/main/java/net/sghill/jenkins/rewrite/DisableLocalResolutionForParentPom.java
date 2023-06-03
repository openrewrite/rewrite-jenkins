package net.sghill.jenkins.rewrite;

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
@Value
@EqualsAndHashCode(callSuper = true)
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
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext ctx) {
                if (isParentTag()) {
                    Tag relativePathTag = Tag.build("<relativePath/>");
                    return AddOrUpdateChild.addOrUpdateChild(tag, relativePathTag, getCursor().getParentOrThrow());
                }
                return super.visitTag(tag, ctx);
            }
        };
    }
}
