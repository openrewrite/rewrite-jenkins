package net.sghill.jenkins.rewrite;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.text.PlainTextParser;

import java.nio.file.PathMatcher;
import java.util.Arrays;
import java.util.List;

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
    protected List<SourceFile> visit(List<SourceFile> before, ExecutionContext ctx) {
        for (SourceFile sourceFile : before) {
            PathMatcher pathMatcher = sourceFile.getSourcePath().getFileSystem()
                    .getPathMatcher("glob:**/src/main/resources/index.jelly");
            if (pathMatcher.matches(sourceFile.getSourcePath())) {
                return before;
            }
        }

        return ListUtils.flatMap(before, sourceFile -> {
            if(Jenkins.isJenkinsPluginPom(sourceFile)) {
                return Arrays.asList(sourceFile, new PlainTextParser().parse("TODO insert contents here")
                        .get(0)
                        .withSourcePath(sourceFile.getSourcePath().resolve("../src/main/resources/index.jelly").normalize()));
            }
            return sourceFile;
        });
    }
}
