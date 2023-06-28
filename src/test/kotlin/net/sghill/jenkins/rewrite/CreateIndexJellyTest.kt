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
package net.sghill.jenkins.rewrite

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions.mavenProject
import org.openrewrite.java.Assertions.srcMainResources
import org.openrewrite.maven.Assertions.pomXml
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest
import org.openrewrite.test.SourceSpecs.other
import org.openrewrite.test.SourceSpecs.text

@Disabled // TODO port to rewrite 8
class CreateIndexJellyTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(CreateIndexJelly())
    }

    @Test
    fun indexJellyAlreadyExists() = rewriteRun(
        other("peanut butter and...") {
            spec -> spec.path("src/main/resources/index.jelly")
        }
    )

    @Test
    fun createIndexJelly() = rewriteRun(
        mavenProject("plugin",
            pomXml("""
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
            """),
            srcMainResources(
                text(null, """
                    <?jelly escape-by-default='true'?>
                    <div>
                    This is my plugin's description
                    </div>
                """) {
                    spec -> spec.path("index.jelly")
                }
            )
        )
    )
    @Test
    fun createIndexJellyEmptyDescription() = rewriteRun(
        mavenProject("plugin",
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
            """),
            srcMainResources(
                text(null, """
                    <?jelly escape-by-default='true'?>
                    <div>
                    my-plugin
                    </div>
                """) {
                    spec -> spec.path("index.jelly")
                }
            )
        )
    )

    @Test
    fun createIndexJellyNoDescription() = rewriteRun(
        mavenProject("plugin",
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
            """),
            srcMainResources(
                text(null, """
                    <?jelly escape-by-default='true'?>
                    <div>
                    my-plugin
                    </div>
                """) {
                    spec -> spec.path("index.jelly")
                }
            )
        )
    )
}
