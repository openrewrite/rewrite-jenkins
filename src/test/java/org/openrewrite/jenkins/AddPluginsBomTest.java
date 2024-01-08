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

import static org.openrewrite.maven.Assertions.pomXml;

class AddPluginsBomTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddPluginsBom());
    }

    @Test
    void shouldNotAddBomIfNoDependencies() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.387.3</jenkins.version>
                </properties>
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
    void shouldNotAddBomIfNoManagedDependencies() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.387.3</jenkins.version>
                </properties>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>com.lmax</groupId>
                        <artifactId>disruptor</artifactId>
                        <version>3.4.4</version>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    @DocumentExample
    void shouldAddBomIfManagedDependencies() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>ant</artifactId>
                        <version>1.9</version>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent(),
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.361.x</artifactId>
                            <version>2102.v854b_fec19c92</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>ant</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    void shouldLeaveBomVersionIfAlreadyPresent() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.361.x</artifactId>
                            <version>1706.vc166d5f429f8</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>ant</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    void shouldFixOutdatedPluginsBom() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.346.x</artifactId>
                            <version>1706.vc166d5f429f8</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.319.x</artifactId>
                            <version>1135.va_4eeca_ea_21c1</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>ant</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent(),
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.361.x</artifactId>
                            <version>2102.v854b_fec19c92</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>ant</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    void shouldFixOutdatedPluginsBomPropertiesBelowManagedDependencies() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.346.x</artifactId>
                            <version>1706.vc166d5f429f8</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>ant</artifactId>
                    </dependency>
                </dependencies>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
            </project>
            """.stripIndent(),
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.361.x</artifactId>
                            <version>2102.v854b_fec19c92</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>ant</artifactId>
                    </dependency>
                </dependencies>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    void shouldFixOutdatedPluginsBomEvenIfUnused() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.346.x</artifactId>
                            <version>1706.vc166d5f429f8</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>com.lmax</groupId>
                        <artifactId>disruptor</artifactId>
                        <version>3.4.4</version>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent(),
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.361.x</artifactId>
                            <version>2102.v854b_fec19c92</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>com.lmax</groupId>
                        <artifactId>disruptor</artifactId>
                        <version>3.4.4</version>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    void shouldLeaveOtherBomsAlone() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.70</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.361.4</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>org.testcontainers</groupId>
                            <artifactId>testcontainers-bom</artifactId>
                            <version>1.18.3</version>
                            <type>pom</type>
                            <scope>import</scope>
                        </dependency>
                    </dependencies>
                </dependencyManagement>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
                <dependencies>
                    <dependency>
                        <groupId>org.testcontainers</groupId>
                        <artifactId>testcontainers</artifactId>
                        <scope>test</scope>
                    </dependency>
                </dependencies>
            </project>
            """.stripIndent()
        ));
    }

    @Test
    void shouldHandleCommentsInManagedDependency() {
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
              <parent>
                <groupId>org.jenkins-ci.plugins</groupId>
                <artifactId>plugin</artifactId>
                <version>4.51</version>
                <relativePath/>
              </parent>

              <artifactId>golang</artifactId>
              <version>1.5-SNAPSHOT</version>
              <packaging>hpi</packaging>

              <properties>
                <jenkins.version>2.346.3</jenkins.version>
              </properties>

              <dependencies>
                <!-- Install Pipeline when running locally, for testing -->
                <dependency>
                  <groupId>org.jenkins-ci.plugins.workflow</groupId>
                  <artifactId>workflow-api</artifactId>
                  <version>2.6</version>
                  <scope>test</scope>
                </dependency>
              </dependencies>

              <dependencyManagement>
                <!-- Simplifies inclusion of plugins: https://github.com/jenkinsci/bom -->
                <dependencies>
                  <dependency>
                    <groupId>io.jenkins.tools.bom</groupId>
                    <artifactId>bom-2.190.x</artifactId>
                    <version>16</version>
                    <type>pom</type>
                    <scope>import</scope>
                  </dependency>
                </dependencies>
              </dependencyManagement>

              <!-- get every artifact through repo.jenkins-ci.org, which proxies all the artifacts that we need -->
              <repositories>
                <repository>
                  <id>repo.jenkins-ci.org</id>
                  <url>https://repo.jenkins-ci.org/public/</url>
                </repository>
              </repositories>

              <pluginRepositories>
                <pluginRepository>
                  <id>repo.jenkins-ci.org</id>
                  <url>https://repo.jenkins-ci.org/public/</url>
                </pluginRepository>
              </pluginRepositories>
            </project>
            """.stripIndent(),
          """
            <project>
              <parent>
                <groupId>org.jenkins-ci.plugins</groupId>
                <artifactId>plugin</artifactId>
                <version>4.51</version>
                <relativePath/>
              </parent>

              <artifactId>golang</artifactId>
              <version>1.5-SNAPSHOT</version>
              <packaging>hpi</packaging>

              <properties>
                <jenkins.version>2.346.3</jenkins.version>
              </properties>

              <dependencies>
                <!-- Install Pipeline when running locally, for testing -->
                <dependency>
                  <groupId>org.jenkins-ci.plugins.workflow</groupId>
                  <artifactId>workflow-api</artifactId>
                  <scope>test</scope>
                </dependency>
              </dependencies>

              <dependencyManagement>
                <!-- Simplifies inclusion of plugins: https://github.com/jenkinsci/bom -->
                <dependencies>
                  <dependency>
                    <groupId>io.jenkins.tools.bom</groupId>
                    <artifactId>bom-2.346.x</artifactId>
                    <version>1763.v092b_8980a_f5e</version>
                    <type>pom</type>
                    <scope>import</scope>
                  </dependency>
                </dependencies>
              </dependencyManagement>

              <!-- get every artifact through repo.jenkins-ci.org, which proxies all the artifacts that we need -->
              <repositories>
                <repository>
                  <id>repo.jenkins-ci.org</id>
                  <url>https://repo.jenkins-ci.org/public/</url>
                </repository>
              </repositories>

              <pluginRepositories>
                <pluginRepository>
                  <id>repo.jenkins-ci.org</id>
                  <url>https://repo.jenkins-ci.org/public/</url>
                </pluginRepository>
              </pluginRepositories>
            </project>
            """.stripIndent()

        ));
    }
}
