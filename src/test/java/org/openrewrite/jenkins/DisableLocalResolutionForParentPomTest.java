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

import static org.openrewrite.maven.Assertions.pomXml;

class DisableLocalResolutionForParentPomTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new DisableLocalResolutionForParentPom());
    }

    @DocumentExample
    @Test
    void shouldAddRelativePathIfMissing() {
        rewriteRun(
          //language=xml
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                  </parent>
                  <artifactId>foo</artifactId>
                  <properties>
                      <jenkins.version>2.303.3</jenkins.version>
                  </properties>
                  <repositories>
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
                      <jenkins.version>2.303.3</jenkins.version>
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
        );
    }

    @Test
    void shouldNoOpIfRelativePathAlreadyPresent() {
        rewriteRun(
          //language=xml
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                      <relativePath/>
                  </parent>
                  <artifactId>foo</artifactId>
                  <properties>
                      <jenkins.version>2.303.3</jenkins.version>
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
        );
    }
}
