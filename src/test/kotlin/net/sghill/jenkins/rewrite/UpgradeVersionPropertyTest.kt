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

import org.junit.jupiter.api.Test
import org.openrewrite.maven.Assertions.pomXml
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class UpgradeVersionPropertyTest : RewriteTest {

    override fun defaults(spec: RecipeSpec) {
        spec.recipe(UpgradeVersionProperty("jenkins.version", "2.364.1"))
    }
    
    @Test
    fun upgrades() = rewriteRun(
        pomXml(
            """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.40</version>
                    <relativePath/>
                </parent>
                <artifactId>example-plugin</artifactId>
                <version>0.8-SNAPSHOT</version>
                <properties>
                    <jenkins.version>2.303.1</jenkins.version>
                </properties>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>http://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """,
            """
                <project>
                    <parent>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>plugin</artifactId>
                        <version>4.40</version>
                        <relativePath/>
                    </parent>
                    <artifactId>example-plugin</artifactId>
                    <version>0.8-SNAPSHOT</version>
                    <properties>
                        <jenkins.version>2.364.1</jenkins.version>
                    </properties>
                    <repositories>
                        <repository>
                            <id>repo.jenkins-ci.org</id>
                            <url>http://repo.jenkins-ci.org/public/</url>
                        </repository>
                    </repositories>
                </project>
            """
        )
    )

    @Test
    fun doesNotDowngrade() = rewriteRun(
            pomXml(
                    """
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>4.67</version>
                                <relativePath/>
                            </parent>
                            <artifactId>example-plugin</artifactId>
                            <version>0.8-SNAPSHOT</version>
                            <properties>
                                <jenkins.version>2.375.1</jenkins.version>
                            </properties>
                            <repositories>
                                <repository>
                                    <id>repo.jenkins-ci.org</id>
                                    <url>http://repo.jenkins-ci.org/public/</url>
                                </repository>
                            </repositories>
                        </project>
                    """            )
    )
}
