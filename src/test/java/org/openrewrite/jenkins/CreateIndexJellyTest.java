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
package org.openrewrite.jenkins;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpec;

import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.java.Assertions.srcMainResources;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.test.SourceSpecs.other;
import static org.openrewrite.test.SourceSpecs.text;

class CreateIndexJellyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new CreateIndexJelly());
    }

    @Test
    void shouldNoOpIfIndexJellyAlreadyExists() {
        rewriteRun(other("peanut butter and...", spec ->
                spec.path("src/main/resources/index.jelly")));
    }

    /*
    The recipe added a source file "src/main/resources/index.jelly" that was not expected.
     */
    @Test
    @Disabled
    void shouldCreateIndexJelly() {
        rewriteRun(
                pomXml(
                        """
                                <project>
                                    <parent>
                                        <groupId>org.jenkins-ci.plugins</groupId>
                                        <artifactId>plugin</artifactId>
                                        <version>4.40</version>
                                    </parent>
                                    <artifactId>my-plugin</artifactId>
                                    <description>This is my plugin's description</description>
                                    <version>0.1</version>
                                    <repositories>
                                        <repository>
                                            <id>repo.jenkins-ci.org</id>
                                            <url>https://repo.jenkins-ci.org/public/</url>
                                        </repository>
                                    </repositories>
                                </project>
                                """.stripIndent()
                ),
                text(
                        null,
                        """
                                <?jelly escape-by-default='true'?>
                                <div>
                                This is my plugin's description
                                </div>
                                """.stripIndent(),
                        spec -> spec.path("src/main/resources/index.jelly")
                )
        );
    }

    /*
    org.opentest4j.AssertionFailedError: [Unexpected result in "src/main/resources/index.jelly"] 
    expected: 
      "<?jelly escape-by-default='true'?>
      <div>
      This is mismatched
      </div>"
     but was: 
      "<?jelly escape-by-default='true'?>
      <div>
      This is my plugin's description
      </div>"
     */
    @Test
    @Disabled
    void shouldCreateIndexJellyExample() {
        rewriteRun(
                pomXml(
                        """
                                <project>
                                    <parent>
                                        <groupId>org.jenkins-ci.plugins</groupId>
                                        <artifactId>plugin</artifactId>
                                        <version>4.40</version>
                                    </parent>
                                    <artifactId>my-plugin</artifactId>
                                    <description>This is my plugin's description</description>
                                    <version>0.1</version>
                                    <repositories>
                                        <repository>
                                            <id>repo.jenkins-ci.org</id>
                                            <url>https://repo.jenkins-ci.org/public/</url>
                                        </repository>
                                    </repositories>
                                </project>
                                """.stripIndent()
                ),
                text(
                        null,
                        """
                                <?jelly escape-by-default='true'?>
                                <div>
                                This is mismatched
                                </div>
                                """.stripIndent(),
                        spec -> spec.path("src/main/resources/index.jelly")
                )
        );
    }

    @Test
    @Disabled
    void shouldCreateIndexJellyEmptyDescription() {
        rewriteRun(mavenProject("plugin",
                pomXml("""
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>4.40</version>
                            </parent>
                            <artifactId>my-plugin</artifactId>
                            <description/>
                            <version>0.1</version>
                            <repositories>
                                <repository>
                                    <id>repo.jenkins-ci.org</id>
                                    <url>https://repo.jenkins-ci.org/public/</url>
                                </repository>
                            </repositories>
                        </project>
                        """.stripIndent()),
                srcMainResources(
                        text(null, """
                                <?jelly escape-by-default='true'?>
                                <div>
                                my-plugin
                                </div>
                                """.stripIndent(), spec -> spec.path("index.jelly"))
                )));
    }

    @Test
    @Disabled
    void shouldCreateIndexJellyNoDescription() {
        rewriteRun(mavenProject("plugin",
                pomXml("""
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>4.40</version>
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
                        """.stripIndent()),
                srcMainResources(
                        text(null, """
                                <?jelly escape-by-default='true'?>
                                <div>
                                my-plugin
                                </div>
                                """.stripIndent(), spec -> spec.path("index.jelly"))
                )));
    }
}
