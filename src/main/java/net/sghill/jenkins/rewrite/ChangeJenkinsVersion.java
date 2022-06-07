package net.sghill.jenkins.rewrite;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.ChangePropertyValue;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

@Value
@EqualsAndHashCode(callSuper = true)
public class ChangeJenkinsVersion extends Recipe {
    String parentPomVersion;
    String jenkinsVersion;

    @Override
    public String getDisplayName() {
        return "Upgrade parent POM and Jenkins version";
    }

    @Override
    public String getDescription() {
        return "This upgrade is atomic so we prevent attempting to resolve a BOM that does not exist yet.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext executionContext) {
                if (isParentTag()) {
                    Tag version = tag.getChild("version")
                            .orElseThrow(() -> new IllegalStateException("Expected to find a version tag for parent"));
                    return new ChangeTagValueVisitor<>(version, parentPomVersion).visitNonNull(tag, executionContext);
                }
                return super.visitTag(tag, executionContext);
            }

            @Override
            public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
                ChangePropertyValue change = new ChangePropertyValue("jenkins.version", jenkinsVersion, true);
                Xml.Document doc = (Xml.Document) change.getVisitor().visitNonNull(document, executionContext);
                return super.visitDocument(doc, executionContext);
            }
        };
    }
}
