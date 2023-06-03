package net.sghill.jenkins.rewrite;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextParser;
import org.openrewrite.xml.search.FindTags;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;

/**
 * Creates the src/main/resources/index.jelly with the project's description
 * if it doesn't exist.
 */
public class CreateIndexJelly extends Recipe {

    @Override
    public String getDisplayName() {
        return "Create `index.jelly` if it doesn't exist";
    }

    @Override
    public String getDescription() {
        return "The Jenkins tooling has changed over the years to [enforce](https://github.com/jenkinsci/maven-hpi-plugin/pull/302) " +
                "that a `src/main/resources/index.jelly` exists with a description. " +
                "If you don't have this, the build fails.";
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
                Xml.Document pom = (Xml.Document) sourceFile;
                String description = FindTags.find(pom, "/project/description").stream()
                        .findAny()
                        .flatMap(Xml.Tag::getValue)
                        .orElse(pom.getMarkers().findFirst(MavenResolutionResult.class)
                                .map(maven -> maven.getPom().getArtifactId())
                                .orElseThrow(() -> new IllegalStateException("Expected to find an artifact id")));

                PlainText indexJelly = new PlainTextParser().parse(String.join("\n",
                                "<?jelly escape-by-default='true'?>",
                                "<div>",
                                description,
                                "</div>"))
                        .get(0)
                        .withSourcePath(sourceFile.getSourcePath().resolve("../src/main/resources/index.jelly").normalize());

//                indexJelly = new AutoFormatVisitor<ExecutionContext>().visitDocument(indexJelly, ctx);
                return Arrays.asList(sourceFile, indexJelly);
            }
            return sourceFile;
        });
    }
}
