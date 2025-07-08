package org.openrewrite.jenkins;

import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.groovy.GroovyParser;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextVisitor;

public class JenkinsfileAsGroovy extends Recipe {

    @Override
    public String getDisplayName() {
        return "Parse `Jenkinsfile` as Groovy";
    }

    @Override
    public String getDescription() {
        return "Parse any `Jenkinsfile` as Groovy code.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new FindSourceFiles("**/Jenkinsfile*"), new PlainTextVisitor<ExecutionContext>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (tree instanceof PlainText) {
                    PlainText pt = (PlainText) tree;
                    return GroovyParser.builder().build()
                            .parse(pt.getText())
                            .findFirst()
                            .map(sourceFile -> sourceFile
                                    .<SourceFile>withId(pt.getId())
                                    .<SourceFile>withMarkers(pt.getMarkers())
                                    .<SourceFile>withSourcePath(pt.getSourcePath()))
                            .orElse(pt);
                }
                return super.visit(tree, ctx);
            }
        });
    }
}
