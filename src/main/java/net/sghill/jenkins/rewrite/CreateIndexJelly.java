package net.sghill.jenkins.rewrite;

import lombok.Getter;
import org.openrewrite.ExecutionContext;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.text.PlainTextParser;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CreateIndexJelly extends Recipe {

    @Override
    public String getDisplayName() {
        return "Create `index.jelly` if it doesn't exist";
    }

    @Override
    public String getDescription() {
        return "The Jenkins tooling has changed over the years to [enforce](https://github.com/jenkinsci/maven-hpi-plugin/pull/302) " +
                "that a `src/main/resources/index.jelly` exists with a description. " +
                "If you don't have this, the build fails";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getApplicableTest() {
        return new IsJenkinsPlugin("*").getVisitor();
    }

    @Override
    protected List<SourceFile> visit(List<SourceFile> before, ExecutionContext ctx) {
        for (SourceFile sourceFile : before) {
            PathMatcher pathMatcher = sourceFile.getSourcePath().getFileSystem()
                    .getPathMatcher("glob:**/src/main/resources/index.jelly");
            if (pathMatcher.matches(sourceFile.getSourcePath())) {
                return before;
            }
        }

        return ListUtils.flatMap(before, sourceFile -> {
            if (Jenkins.isJenkinsPluginPom(sourceFile) != null) { // This must be an `Xml.Document`
                DescriptionVisitor visitor = new DescriptionVisitor();
                visitor.visitDocument((Xml.Document) sourceFile, new InMemoryExecutionContext());
                return Arrays.asList(sourceFile, new PlainTextParser().parse(String.join("\n",
                                "<?jelly escape-by-default='true'?>",
                                "<div>",
                                visitor.description(),
                                "</div>"))
                        .get(0)
                        .withSourcePath(sourceFile.getSourcePath().resolve("../src/main/resources/index.jelly").normalize()));
            }
            return sourceFile;
        });
    }

    static class DescriptionVisitor extends MavenVisitor<ExecutionContext> {
        private static final String FALLBACK = "Update src/main/resources/index.jelly for a better description.";

        private String value = "";

        public String description() {
            return value.isEmpty() ? FALLBACK : value;
        }

        private static final XPathMatcher DESCRIPTION_MATCHER = new XPathMatcher("/project/description");
        private static final XPathMatcher ARTIFACT_ID_MATCHER = new XPathMatcher("/project/artifactId");

        @Override
        public Xml visitTag(Xml.Tag tag, ExecutionContext executionContext) {
            Optional<String> tagValue = tag.getValue();
            boolean isPresent = tagValue.isPresent();
            if (DESCRIPTION_MATCHER.matches(getCursor()) && isPresent) {
                value = tagValue.get();
                return tag;
            }
            if (ARTIFACT_ID_MATCHER.matches(getCursor()) && isPresent) {
                value = tagValue.get();
            }
            return super.visitTag(tag, executionContext);
        }
    }
}
