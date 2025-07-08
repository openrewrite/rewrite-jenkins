/*
 * Copyright 2025 the original author or authors.
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
