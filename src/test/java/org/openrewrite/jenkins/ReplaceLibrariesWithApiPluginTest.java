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

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.util.Set;

import static org.openrewrite.maven.Assertions.pomXml;

class ReplaceLibrariesWithApiPluginTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new ReplaceLibrariesWithApiPlugin(
          "io.jenkins.plugins",
          "commons-text-api",
          "1.9-5.v7ea_44fe6061c",
          Set.of(new ReplaceLibrariesWithApiPlugin.Library("org.apache.commons", "commons-text"))
        ));
    }

    @Test
    void shouldWorkFromYamlDefinition() {
        rewriteRun(spec -> spec.recipeFromResource(
          "/replace-libraries-with-api-plugin.yml",
          "org.openrewrite.jenkins.CommonsTextToApiPlugin"
        ), pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.86</version>
                    <relativePath />
                </parent>

                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>org.apache.commons</groupId>
                        <artifactId>commons-text</artifactId>
                        <version>1.9</version>
                    </dependency>
                </dependencies>

                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """.stripIndent(),
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.86</version>
                    <relativePath />
                </parent>

                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>io.jenkins.plugins</groupId>
                        <artifactId>commons-text-api</artifactId>
                        <version>1.9-5.v7ea_44fe6061c</version>
                    </dependency>
                </dependencies>

                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    void shouldReplaceDirectDependencyWithApiPlugin() {
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.86</version>
                    <relativePath />
                </parent>

                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>org.apache.commons</groupId>
                        <artifactId>commons-text</artifactId>
                        <version>1.9</version>
                    </dependency>
                </dependencies>

                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """.stripIndent(),
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.86</version>
                    <relativePath />
                </parent>

                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>io.jenkins.plugins</groupId>
                        <artifactId>commons-text-api</artifactId>
                        <version>1.9-5.v7ea_44fe6061c</version>
                    </dependency>
                </dependencies>

                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    @DocumentExample
    void shouldExcludeTransitivesFromBundledLibrary() {
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.86</version>
                    <relativePath />
                </parent>

                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>org.apache.turbine</groupId>
                        <artifactId>turbine</artifactId>
                        <version>5.1</version>
                    </dependency>
                </dependencies>

                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """.stripIndent(),
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.86</version>
                    <relativePath />
                </parent>

                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>

                <dependencies>
                    <dependency>
                        <groupId>io.jenkins.plugins</groupId>
                        <artifactId>commons-text-api</artifactId>
                        <version>1.9-5.v7ea_44fe6061c</version>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.turbine</groupId>
                        <artifactId>turbine</artifactId>
                        <version>5.1</version>
                        <exclusions>
                            <exclusion>
                                <!-- brought in by io.jenkins.plugins:commons-text-api -->
                                <groupId>org.apache.commons</groupId>
                                <artifactId>commons-text</artifactId>
                            </exclusion>
                        </exclusions>
                    </dependency>
                </dependencies>

                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """.stripIndent()
        ));
    }
}
