/*
 * Copyright 2024 the original author or authors.
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

    @DocumentExample
    @Test
    void shouldExcludeTransitivesFromBundledLibrary() {
        rewriteRun(
          //language=xml
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                      <relativePath />
                  </parent>
                  <artifactId>foo</artifactId>

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
                          <id>maven-central</id>
                          <url>https://repo1.maven.org/maven2/</url>
                      </repository>
                      <repository>
                          <id>repo.jenkins-ci.org</id>
                          <url>https://repo.jenkins-ci.org/public/</url>
                      </repository>
                  </repositories>
              </project>
              """,
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                      <relativePath />
                  </parent>
                  <artifactId>foo</artifactId>

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
                          <id>maven-central</id>
                          <url>https://repo1.maven.org/maven2/</url>
                      </repository>
                      <repository>
                          <id>repo.jenkins-ci.org</id>
                          <url>https://repo.jenkins-ci.org/public/</url>
                      </repository>
                  </repositories>
              </project>
              """
          )
        );
    }

    @Test
    void shouldWorkFromYamlDefinition() {
        rewriteRun(spec -> spec.recipeFromResource(
            "/replace-libraries-with-api-plugin.yml",
            "org.openrewrite.jenkins.CommonsTextToApiPlugin"
          ),
          //language=xml
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                      <relativePath />
                  </parent>
                  <artifactId>foo</artifactId>

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
                          <id>maven-central</id>
                          <url>https://repo1.maven.org/maven2/</url>
                      </repository>
                      <repository>
                          <id>repo.jenkins-ci.org</id>
                          <url>https://repo.jenkins-ci.org/public/</url>
                      </repository>
                  </repositories>
              </project>
              """,
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                      <relativePath />
                  </parent>
                  <artifactId>foo</artifactId>

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
                          <id>maven-central</id>
                          <url>https://repo1.maven.org/maven2/</url>
                      </repository>
                      <repository>
                          <id>repo.jenkins-ci.org</id>
                          <url>https://repo.jenkins-ci.org/public/</url>
                      </repository>
                  </repositories>
              </project>
              """
          )
        );
    }

    @Test
    void shouldReplaceDirectDependencyWithApiPlugin() {
        rewriteRun(
          //language=xml
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                      <relativePath />
                  </parent>
                  <artifactId>foo</artifactId>

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
                          <id>maven-central</id>
                          <url>https://repo1.maven.org/maven2/</url>
                      </repository>
                      <repository>
                          <id>repo.jenkins-ci.org</id>
                          <url>https://repo.jenkins-ci.org/public/</url>
                      </repository>
                  </repositories>
              </project>
              """,
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                      <relativePath />
                  </parent>
                  <artifactId>foo</artifactId>

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
                          <id>maven-central</id>
                          <url>https://repo1.maven.org/maven2/</url>
                      </repository>
                      <repository>
                          <id>repo.jenkins-ci.org</id>
                          <url>https://repo.jenkins-ci.org/public/</url>
                      </repository>
                  </repositories>
              </project>
              """
          )
        );
    }
}
