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
import org.openrewrite.maven.Assertions.pomXml
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class ModernizeJenkinsPluginTest : RewriteTest {

    override fun defaults(spec: RecipeSpec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml", "net.sghill.jenkins.ModernizePlugin")
    }

    @Test
    fun majorVersionUpgrade() = rewriteRun(
            pomXml(
                    """
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>3.40</version>
                            </parent>
                            <artifactId>permissive-script-security</artifactId>
                            <version>0.8-SNAPSHOT</version>
                            <properties>
                                <java.level>8</java.level>
                                <jenkins.version>2.107.3</jenkins.version>
                            </properties>
                            <repositories>
                                <repository>
                                    <id>repo.jenkins-ci.org</id>
                                    <url>http://repo.jenkins-ci.org/public/</url>
                                </repository>
                            </repositories>
                        </project>
                    """, """
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>4.64</version>
                                <relativePath/>
                            </parent>
                            <artifactId>permissive-script-security</artifactId>
                            <version>0.8-SNAPSHOT</version>
                            <properties>
                                <jenkins.version>2.332.1</jenkins.version>
                            </properties>
                            <repositories>
                                <repository>
                                    <id>repo.jenkins-ci.org</id>
                                    <url>https://repo.jenkins-ci.org/public/</url>
                                </repository>
                            </repositories>
                        </project>
                    """
            )
    )

    @Test
    @Disabled
    fun pluginInBom() = rewriteRun(
            pomXml(
                    """
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>4.42</version>
                                <relativePath />
                            </parent>
                            <artifactId>permissive-script-security</artifactId>
                            <version>0.8-SNAPSHOT</version>
                            <properties>
                                <jenkins.version>2.303.3</jenkins.version>
                            </properties>
                            <dependencies>
                                <dependency>
                                    <groupId>org.jenkins-ci.plugins</groupId>
                                    <artifactId>junit</artifactId>
                                    <version>1.12</version>
                                </dependency>
                            </dependencies>
                            <repositories>
                                <repository>
                                    <id>repo.jenkins-ci.org</id>
                                    <url>https://repo.jenkins-ci.org/public/</url>
                                </repository>
                            </repositories>
                        </project>
                    ""","""
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>4.42</version>
                                <relativePath />
                            </parent>
                            <artifactId>permissive-script-security</artifactId>
                            <version>0.8-SNAPSHOT</version>
                            <properties>
                                <jenkins.version>2.303.3</jenkins.version>
                            </properties>
                            <dependencyManagement>
                                <dependencies>
                                    <dependency>
                                        <groupId>io.jenkins.tools.bom</groupId>
                                        <artifactId>bom-2.303.x</artifactId>
                                        <version>1409.v7659b_c072f18</version>
                                        <type>pom</type>
                                        <scope>import</scope>
                                    </dependency>
                                </dependencies>
                            </dependencyManagement>
                            <dependencies>
                                <dependency>
                                    <groupId>org.jenkins-ci.plugins</groupId>
                                    <artifactId>junit</artifactId>
                                </dependency>
                            </dependencies>
                            <repositories>
                                <repository>
                                    <id>repo.jenkins-ci.org</id>
                                    <url>https://repo.jenkins-ci.org/public/</url>
                                </repository>
                            </repositories>
                        </project>
                        """,
            )
    )
}