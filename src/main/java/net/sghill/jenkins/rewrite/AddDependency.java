package net.sghill.jenkins.rewrite;

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
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
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
