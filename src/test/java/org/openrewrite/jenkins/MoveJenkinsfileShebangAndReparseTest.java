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

import org.junit.jupiter.api.Test;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.ParseExceptionResult;
import org.openrewrite.Result;
import org.openrewrite.SourceFile;
import org.openrewrite.groovy.GroovyParser;
import org.openrewrite.groovy.tree.G;
import org.openrewrite.internal.InMemoryLargeSourceSet;
import org.openrewrite.tree.ParseError;

import java.nio.file.Paths;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class MoveJenkinsfileShebangAndReparseTest {

    @Test
    void recoversMisplacedShebang() {
        //language=groovy
        String broken = """
                // Refer below link more Jenkinsfile Params info
                #!groovy
                @Library('foo') _
                myPipeline { stage('build') }
                """;

        SourceFile ingested = ingest(broken);
        assertThat(ingested).isInstanceOf(ParseError.class);
        assertThat(((ParseError) ingested).getMarkers().findFirst(ParseExceptionResult.class))
                .hasValueSatisfying(per -> assertThat(per.getParserType()).isEqualTo("GroovyParser"));

        SourceFile after = runRecipe(ingested);

        assertThat(after)
                .isNotInstanceOf(ParseError.class)
                .isInstanceOf(G.CompilationUnit.class);
        assertThat(after.printAll()).isEqualTo("""
                #!groovy
                // Refer below link more Jenkinsfile Params info
                @Library('foo') _
                myPipeline { stage('build') }
                """);
    }

    @Test
    void leavesUnrelatedJenkinsfileParseErrorAlone() {
        //language=groovy
        String broken = """
                @Library('foo') _
                myPipeline {
                    sh "echo unclosed
                }
                """;

        SourceFile ingested = ingest(broken);
        assertThat(ingested).isInstanceOf(ParseError.class);

        List<Result> results = new MoveJenkinsfileShebangAndReparse()
                .run(new InMemoryLargeSourceSet(singletonList(ingested)), new InMemoryExecutionContext())
                .getChangeset().getAllResults();

        assertThat(results).isEmpty();
    }

    @Test
    void leavesNonJenkinsfileAlone() {
        //language=groovy
        String broken = """
                // comment
                #!groovy
                def a = 'hello'
                """;

        SourceFile ingested = GroovyParser.builder().build()
                .parse(broken)
                .findFirst()
                .orElseThrow()
                .withSourcePath(Paths.get("not-a-jenkinsfile.groovy"));
        assertThat(ingested).isInstanceOf(ParseError.class);

        List<Result> results = new MoveJenkinsfileShebangAndReparse()
                .run(new InMemoryLargeSourceSet(singletonList(ingested)), new InMemoryExecutionContext())
                .getChangeset().getAllResults();

        assertThat(results).isEmpty();
    }

    private static SourceFile ingest(String source) {
        return GroovyParser.builder().build()
                .parse(source)
                .findFirst()
                .orElseThrow()
                .withSourcePath(Paths.get("Jenkinsfile"));
    }

    private static SourceFile runRecipe(SourceFile ingested) {
        List<Result> results = new MoveJenkinsfileShebangAndReparse()
                .run(new InMemoryLargeSourceSet(singletonList(ingested)), new InMemoryExecutionContext())
                .getChangeset().getAllResults();
        assertThat(results).hasSize(1);
        return results.get(0).getAfter();
    }
}
