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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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

    /**
     * This is biased toward recency.
     * If we don't recognize the version as a LTS we assume it is very recent and wants the weekly bom.
     */
    @ParameterizedTest
    @MethodSource("versionToBom")
    void shouldGenerateBomNameFromJenkinsVersion(String jenkinsVersion, String bomVersion) {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        scanned.jenkinsVersion = jenkinsVersion;

        String actual = scanned.bomName();

        assertThat(actual).isEqualTo(bomVersion);
    }

    @Test
    void shouldNeedPluginsBomIfAbsent() {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        scanned.jenkinsVersion = "2.263.4";
        scanned.foundPlugins.add(new AddPluginsBom.Artifact("org.jenkins-ci.jpi", "ant"));

        assertThat(scanned.needsPluginsBom()).isTrue();
    }

    @Test
    void shouldNeedPluginsBomIfOutdatedAndNoFoundPlugins() {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        scanned.jenkinsVersion = "2.263.4";
        scanned.foundPluginsBoms.add(new AddPluginsBom.Artifact("io.jenkins.tools.bom", "bom-2.319.x"));

        assertThat(scanned.needsPluginsBom()).isTrue();
    }

    @Test
    void shouldNeedPluginsBomIfOnlyMismatchedPresent() {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        scanned.jenkinsVersion = "2.263.4";
        scanned.foundPlugins.add(new AddPluginsBom.Artifact("org.jenkins-ci.jpi", "ant"));
        scanned.foundPluginsBoms.add(new AddPluginsBom.Artifact("io.jenkins.tools.bom", "bom-2.319.x"));

        assertThat(scanned.needsPluginsBom()).isTrue();
    }

    @Test
    void shouldNeedPluginsBomIfMultipleBomsPresent() {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        scanned.jenkinsVersion = "2.263.4";
        scanned.foundPlugins.add(new AddPluginsBom.Artifact("org.jenkins-ci.jpi", "ant"));
        scanned.foundPluginsBoms.add(new AddPluginsBom.Artifact("io.jenkins.tools.bom", "bom-2.319.x"));
        scanned.foundPluginsBoms.add(new AddPluginsBom.Artifact("io.jenkins.tools.bom", scanned.bomName()));

        assertThat(scanned.needsPluginsBom()).isTrue();
    }

    @Test
    void shouldNotNeedPluginsBomIfExpectedVersionAlreadyApplied() {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        scanned.jenkinsVersion = "2.263.4";
        scanned.foundPlugins.add(new AddPluginsBom.Artifact("org.jenkins-ci.jpi", "ant"));
        scanned.foundPluginsBoms.add(new AddPluginsBom.Artifact("io.jenkins.tools.bom", scanned.bomName()));

        assertThat(scanned.needsPluginsBom()).isFalse();
    }

    @Test
    void shouldNotNeedPluginsBomIfNoJenkinsVersion() {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        scanned.foundPlugins.add(new AddPluginsBom.Artifact("org.jenkins-ci.jpi", "ant"));

        assertThat(scanned.bomToChange()).isNull();
        assertThat(scanned.needsPluginsBom()).isFalse();
    }

    @Test
    void shouldChangeABomToMinimizeCommentAndTagOrderingMoves() {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        AddPluginsBom.Artifact two319 = new AddPluginsBom.Artifact("io.jenkins.tools.bom", "bom-2.319.x");
        AddPluginsBom.Artifact two332 = new AddPluginsBom.Artifact("io.jenkins.tools.bom", "bom-2.332.x");
        scanned.foundPluginsBoms.add(two319);
        scanned.foundPluginsBoms.add(two332);

        Set<AddPluginsBom.Artifact> actual = scanned.bomsToRemove();

        assertThat(scanned.bomToChange()).isEqualTo(two319);
        assertThat(actual).containsExactly(two332);
    }

    @Test
    void shouldFilterExpectedBomOutOfBomsToRemove() {
        AddPluginsBom.Scanned scanned = new AddPluginsBom.Scanned();
        AddPluginsBom.Artifact two319 = new AddPluginsBom.Artifact("io.jenkins.tools.bom", "bom-2.319.x");
        AddPluginsBom.Artifact two332 = new AddPluginsBom.Artifact("io.jenkins.tools.bom", "bom-2.332.x");
        scanned.foundPluginsBoms.add(two319);
        scanned.foundPluginsBoms.add(new AddPluginsBom.Artifact("io.jenkins.tools.bom", scanned.bomName()));
        scanned.foundPluginsBoms.add(two332);

        Set<AddPluginsBom.Artifact> actual = scanned.bomsToRemove();

        assertThat(actual).containsExactlyInAnyOrder(two319, two332);
    }

    static Stream<Arguments> versionToBom() {
        return Stream.of(
                arguments("2.277.3", "bom-2.277.x"),
                arguments("2.319.1", "bom-2.319.x"),
                arguments("2.361.4", "bom-2.361.x"),
                arguments("2.401.2", "bom-2.401.x"),
                arguments("2.384", "bom-weekly"),
                arguments("2.401", "bom-weekly"),
                arguments("888888-SNAPSHOT", "bom-weekly"),
                arguments("2.379-rc33114.2f90818f6a_35", "bom-weekly")
        );
    }
}
