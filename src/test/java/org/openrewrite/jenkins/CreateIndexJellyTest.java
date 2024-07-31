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

    @Test
    @DocumentExample
    void shouldCreateIndexJellyFromPomDescription() {
        rewriteRun(
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
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
            s -> s.path("src/main/resources/index.jelly")
          )
        );
    }

    @Test
    void shouldCreateIndexJellyEmptyDescription() {
        rewriteRun(
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
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
              """.stripIndent()
          ),
          text(
            null,
            """
              <?jelly escape-by-default='true'?>
              <div>
              my-plugin
              </div>
              """.stripIndent(),
            s -> s.path("src/main/resources/index.jelly")
          )
        );
    }

    @Test
    void shouldCreateIndexJellyNoDescription() {
        rewriteRun(
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
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
              """.stripIndent()
          ),
          text(
            null,
            """
              <?jelly escape-by-default='true'?>
              <div>
              my-plugin
              </div>
              """.stripIndent(),
            s -> s.path("src/main/resources/index.jelly")
          )
        );
    }

    @Test
    void shouldCreateMultipleNestedIndexJellies() {
        rewriteRun(
          mavenProject("my-root",
            pomXml("""
              <project>
                  <groupId>org.example</groupId>
                  <artifactId>my-root</artifactId>
                  <version>0.1</version>
                  <packaging>pom</packaging>
                  <modules>
                      <module>plugin</module>
                      <module>different-plugin</module>
                      <module>non-plugin</module>
                  </modules>
              </project>
              """.stripIndent()),
            mavenProject("plugin",
              pomXml("""
                <project>
                    <parent>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>plugin</artifactId>
                        <version>4.86</version>
                    </parent>
                    <artifactId>my-plugin</artifactId>
                    <version>0.1</version>
                    <description>This is my plugin</description>
                    <repositories>
                        <repository>
                            <id>repo.jenkins-ci.org</id>
                            <url>https://repo.jenkins-ci.org/public/</url>
                        </repository>
                    </repositories>
                </project>
                """.stripIndent()),
              srcMainResources(
                text(null,
                  """
                    <?jelly escape-by-default='true'?>
                    <div>
                    This is my plugin
                    </div>
                    """.stripIndent(),
                  s -> s.path("index.jelly"))
              )),
            mavenProject("different-plugin",
              pomXml("""
                <project>
                    <parent>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>plugin</artifactId>
                        <version>4.86</version>
                    </parent>
                    <artifactId>different-plugin</artifactId>
                    <version>0.1</version>
                    <description>This is my second, different plugin</description>
                    <repositories>
                        <repository>
                            <id>repo.jenkins-ci.org</id>
                            <url>https://repo.jenkins-ci.org/public/</url>
                        </repository>
                    </repositories>
                </project>
                """.stripIndent()),
              srcMainResources(
                text(null,
                  """
                    <?jelly escape-by-default='true'?>
                    <div>
                    This is my second, different plugin
                    </div>
                    """.stripIndent(),
                  s -> s.path("index.jelly"))
              )),
            mavenProject("non-plugin",
              pomXml("""
                <project>
                    <groupId>org.example</groupId>
                    <artifactId>non-plugin</artifactId>
                    <version>0.1</version>
                    <description>This is my non-plugin</description>
                </project>
                """.stripIndent())))
        );
    }
}
