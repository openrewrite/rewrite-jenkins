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
import static org.openrewrite.test.SourceSpecs.text;

class ModernizeJenkinsfileTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml", "org.openrewrite.jenkins.ModernizeJenkinsfile");
    }

    @Test
    void shouldCreateJenkinsfile() {
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
              """),
          //language=groovy
          text(null,
            """
              /*
               See the documentation for more options:
               https://github.com/jenkins-infra/pipeline-library/
              */ buildPlugin(
                useContainerAgent: true, // Set to `false` if you need to use Docker for containerized tests
                configurations: [
                  [platform: 'linux', jdk: 21],
                  [platform: 'windows', jdk: 17],
              ])
              """,
            spec -> spec.path("Jenkinsfile")));
    }

    @Test
    @DocumentExample
    void shouldUpdateJenkinsfile() {
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
              """
          ),
          //language=groovy
          text("buildPlugin()",
            """
              /*
               See the documentation for more options:
               https://github.com/jenkins-infra/pipeline-library/
              */ buildPlugin(
                useContainerAgent: true, // Set to `false` if you need to use Docker for containerized tests
                configurations: [
                  [platform: 'linux', jdk: 21],
                  [platform: 'windows', jdk: 17],
              ])
              """,
            spec -> spec.noTrim().path("Jenkinsfile")));
    }
}
