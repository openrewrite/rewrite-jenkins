/*
 * Copyright 2026 the original author or authors.
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
import org.jspecify.annotations.Nullable;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.groovy.GroovyParser;
import org.openrewrite.tree.ParseError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = false)
@Value
public class MoveJenkinsfileShebangAndReparse extends Recipe {

    // Anchored to start-of-line via (?m); only matches the shebang line itself plus its terminator.
    private static final Pattern SHEBANG_LINE = Pattern.compile("(?m)^#![^\\r\\n]*(?:\\r\\n|\\r|\\n)?");

    String displayName = "Recover `Jenkinsfile` parse errors caused by a misplaced shebang";

    String description = "Groovy's Antlr4 parser rejects a `#!` shebang that is not on the first line of " +
            "the file, causing the `Jenkinsfile` to be ingested as a `ParseError`. This recipe detects that " +
            "exact failure mode, relocates the shebang to line 1, and re-parses the result with the Groovy " +
            "parser so downstream recipes have a usable Groovy LST to work with.";

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                if (!(tree instanceof ParseError)) {
                    return tree;
                }
                ParseError pe = (ParseError) tree;
                if (!pe.getSourcePath().getFileName().toString().startsWith("Jenkinsfile")) {
                    return pe;
                }
                boolean fromGroovyParser = pe.getMarkers().findFirst(ParseExceptionResult.class)
                        .map(per -> "GroovyParser".equals(per.getParserType()))
                        .orElse(false);
                if (!fromGroovyParser) {
                    return pe;
                }
                String src = pe.getText();
                Matcher m = SHEBANG_LINE.matcher(src);
                if (!m.find() || m.start() == 0) {
                    return pe;
                }
                String shebangLine = m.group();
                String rewritten = shebangLine + src.substring(0, m.start()) + src.substring(m.end());

                return GroovyParser.builder().build()
                        .parse(rewritten)
                        .findFirst()
                        .filter(sf -> !(sf instanceof ParseError))
                        .map(sf -> (Tree) sf
                                .<SourceFile>withId(pe.getId())
                                .<SourceFile>withSourcePath(pe.getSourcePath())
                                .<SourceFile>withFileAttributes(pe.getFileAttributes())
                                .<SourceFile>withCharset(pe.getCharset()))
                        .orElse(pe);
            }
        };
    }
}
