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

import static org.assertj.core.api.Assertions.assertThat;
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
                    <version>4.86</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>
                <repositories>
                    <repository>
                        <id>repo.jenkins-ci.org</id>
                        <url>https://repo.jenkins-ci.org/public/</url>
                    </repository>
                </repositories>
            </project>
            """
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
                    <version>4.86</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
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
            """
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
                    <version>4.86</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
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
            """,
          spec -> spec.after(after -> {
              ModernizePluginTest.Versions versionsAfter = ModernizePluginTest.Versions.parse(after);
              assertThat(versionsAfter.bomArtifactId()).isNotEmpty();
              assertThat(versionsAfter.bomVersion()).isNotEmpty();
              return after;
          })
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
                    <version>4.86</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>bom-2.440.x</artifactId>
                            <version>3221.ve8f7b_fdd149d</version>
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
            """
        ));
    }

    @Test
    void shouldFixOutdatedPluginsBom() {
        String bomArtifactId = "bom-2.346.x";
        String bomVersion = "1706.vc166d5f429f8";
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.86</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>%s</artifactId>
                            <version>%s</version>
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
            """.formatted(bomArtifactId, bomVersion),
          spec -> spec.after(after -> {
              ModernizePluginTest.Versions versionsAfter = ModernizePluginTest.Versions.parse(after);
              assertThat(versionsAfter.bomArtifactId()).isGreaterThan(bomArtifactId);
              assertThat(versionsAfter.bomVersion()).isGreaterThan(bomVersion);
              return after;
          })
        ));
    }

    @Test
    void shouldFixOutdatedPluginsBomPropertiesBelowManagedDependencies() {
        ModernizePluginTest.Versions versionsBefore = new ModernizePluginTest.Versions(
          "4.86",
          "2.440.3",
          "bom-2.346.x",
          "1706.vc166d5f429f8"
        );
        // language=xml
        rewriteRun(pomXml(
            versionsBefore.asPomXml(),
            spec -> spec.after(after -> {
                ModernizePluginTest.Versions versionsAfter = ModernizePluginTest.Versions.parse(after);
                assertThat(versionsAfter.parentVersion()).isGreaterThanOrEqualTo(versionsBefore.parentVersion());
                assertThat(versionsAfter.propertyVersion()).isGreaterThanOrEqualTo(versionsBefore.propertyVersion());
                assertThat(versionsAfter.bomArtifactId()).isGreaterThan(versionsBefore.bomArtifactId());
                assertThat(versionsAfter.bomVersion()).isGreaterThan(versionsBefore.bomVersion());
                return versionsAfter.asPomXml();
            })
          )
        );
    }

    @Test
    void shouldFixOutdatedPluginsBomEvenIfUnused() {
        String bomArtifactId = "bom-2.346.x";
        String bomVersion = "1706.vc166d5f429f8";
        // language=xml
        rewriteRun(pomXml(
          """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.86</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
                </properties>
                <dependencyManagement>
                    <dependencies>
                        <dependency>
                            <groupId>io.jenkins.tools.bom</groupId>
                            <artifactId>%s</artifactId>
                            <version>%s</version>
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
            """.formatted(bomArtifactId, bomVersion),
          spec -> spec.after(after -> {
              ModernizePluginTest.Versions versionsAfter = ModernizePluginTest.Versions.parse(after);
              assertThat(versionsAfter.bomArtifactId()).isGreaterThan(bomArtifactId);
              assertThat(versionsAfter.bomVersion()).isGreaterThan(bomVersion);
              return after;
          })
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
                    <version>4.86</version>
                    <relativePath/>
                </parent>
                <properties>
                    <jenkins.version>2.440.3</jenkins.version>
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
            """
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
                <version>4.86</version>
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
            """,
          """
            <project>
              <parent>
                <groupId>org.jenkins-ci.plugins</groupId>
                <artifactId>plugin</artifactId>
                <version>4.86</version>
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
            """

        ));
    }
}
