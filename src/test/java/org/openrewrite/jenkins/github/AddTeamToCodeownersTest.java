/*
 * Copyright 2023 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.jenkins.github;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.test.SourceSpecs.text;

class AddTeamToCodeownersTest implements RewriteTest {

    @Language("xml")
    // language=xml
    private static final String POM = """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.72</version>
                </parent>
                <artifactId>sample</artifactId>
                <version>0.1</version>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """.stripIndent();

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddTeamToCodeowners());
    }

    @Test
    void shouldAddFileIfMissing() {
        rewriteRun(
                pomXml(POM),
                text(null,
                        """
                                * @jenkinsci/sample-plugin-developers
                                """.stripIndent(),
                        s -> s.path(".github/CODEOWNERS")
                )
        );
    }

    @Test
    void shouldAddLineIfTeamNotDefinedForAll() {
        rewriteRun(
                pomXml(POM),
                text(
                        """
                                # This is a comment.
                                *       @global-owner1 @global-owner2
                                *.js    @js-owner #This is an inline comment.
                                /build/logs/ @doctocat
                                """.stripIndent(),
                        """
                                # This is a comment.
                                *       @jenkinsci/sample-plugin-developers
                                *       @global-owner1 @global-owner2
                                *.js    @js-owner #This is an inline comment.
                                /build/logs/ @doctocat
                                """.stripIndent(),
                        s -> s.path(".github/CODEOWNERS")
                )
        );
    }

    @Test
    void shouldHandleMultiModule() {
        rewriteRun(
                mavenProject("sample-parent",
                        pomXml("""
                                <project>
                                    <groupId>org.example</groupId>
                                    <artifactId>sample-parent</artifactId>
                                    <version>0.1</version>
                                    <packaging>pom</packaging>
                                    <modules>
                                        <module>plugin</module>
                                        <module>different-plugin</module>
                                    </modules>
                                </project>
                                """.stripIndent()),
                        mavenProject("plugin",
                                pomXml("""
                                        <project>
                                            <parent>
                                                <groupId>org.jenkins-ci.plugins</groupId>
                                                <artifactId>plugin</artifactId>
                                                <version>4.72</version>
                                            </parent>
                                            <artifactId>my-plugin</artifactId>
                                            <version>0.1</version>
                                            <repositories>
                                                <repository>
                                                    <id>repo.jenkins-ci.org</id>
                                                    <url>https://repo.jenkins-ci.org/public/</url>
                                                </repository>
                                            </repositories>
                                        </project>
                                        """.stripIndent())),
                        mavenProject("different-plugin",
                                pomXml("""
                                        <project>
                                            <parent>
                                                <groupId>org.jenkins-ci.plugins</groupId>
                                                <artifactId>plugin</artifactId>
                                                <version>4.72</version>
                                            </parent>
                                            <artifactId>different-plugin</artifactId>
                                            <version>0.1</version>
                                            <repositories>
                                                <repository>
                                                    <id>repo.jenkins-ci.org</id>
                                                    <url>https://repo.jenkins-ci.org/public/</url>
                                                </repository>
                                            </repositories>
                                        </project>
                                        """.stripIndent()))),
                text(
                        null,
                        """
                                * @jenkinsci/sample-plugin-developers
                                """.stripIndent(),
                        s -> s.path(".github/CODEOWNERS")
                ));
    }

    @Test
    void shouldNoOpIfTeamAlreadyDefinedForAll() {
        rewriteRun(
                pomXml(POM),
                text(
                        "* @jenkinsci/sample-plugin-developers",
                        s -> s.path(".github/CODEOWNERS")
                )
        );
    }
}
