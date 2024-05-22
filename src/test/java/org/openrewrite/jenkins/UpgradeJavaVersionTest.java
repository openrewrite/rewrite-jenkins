/*
 * Copyright 2024 the original author or authors.
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
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.groovy.Assertions.groovy;

public class UpgradeJavaVersionTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new UpgradeJavaVersion(17, null));
    }

    @Test
    void openJdk() {
        rewriteRun(
          //language=groovy
          groovy("""
              #!/usr/bin/env groovy
              
              stage("Checkout") {
                  scmCheckout {
                      java_version = "openjdk11"
                      deleteWorkspace = 'false'
                  }
              }
              """,
             """
              #!/usr/bin/env groovy
              
              stage("Checkout") {
                  scmCheckout {
                      java_version = "openjdk17"
                      deleteWorkspace = 'false'
                  }
              }
              """,
            spec -> spec.path("Jenkinsfile"))
        );
    }

    @Test
    void jdk() {
        rewriteRun(
          spec -> spec.recipe(new UpgradeJavaVersion(17, "openjdk")),
          //language=groovy
          groovy("""
              node('cicd-build') {
                  stage ("Titan") {
                      titan {
                          gitSource = "github"
                          projectType = "java"
                          mavenVersion = '3.5'
                          javaVersion = 'jdk11'
                          minimumCodeCoverage = 86
                          codeCoverageFilePath = "/target/site/jacoco/index.html"
                      }
                  }
              }
              """,
              """
              node('cicd-build') {
                  stage ("Titan") {
                      titan {
                          gitSource = "github"
                          projectType = "java"
                          mavenVersion = '3.5'
                          javaVersion = 'openjdk17'
                          minimumCodeCoverage = 86
                          codeCoverageFilePath = "/target/site/jacoco/index.html"
                      }
                  }
              }
              """,
            spec -> spec.path("Jenkinsfile"))
        );
    }
}
