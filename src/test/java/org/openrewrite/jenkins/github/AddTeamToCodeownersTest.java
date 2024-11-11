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
package org.openrewrite.jenkins.github;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.mavenProject;
import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.test.SourceSpecs.text;

class AddTeamToCodeownersTest implements RewriteTest {

    @Language("xml")
    // language=xml
    private static final String POM = """
      <project>
          <parent>
              <groupId>org.jenkins-ci.plugins</groupId>
              <artifactId>plugin</artifactId>
              <version>4.86</version>
          </parent>
          <artifactId>sample</artifactId>
          <version>0.1</version>
          <repositories>
              <repository>
                  <id>repo.jenkins-ci.org</id>
                  <url>https://repo.jenkins-ci.org/public/</url>
              </repository>
          </repositories>
      </project>
      """;

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddTeamToCodeowners());
    }

    @Test
    @DocumentExample
    void shouldAddFileIfMissing() {
        rewriteRun(
          pomXml(POM),
          text(null,
            """
              * @jenkinsci/sample-plugin-developers
              """,
            s -> s.path(".github/CODEOWNERS").noTrim()
          )
        );
    }

    @Test
    void shouldAddLineIfTeamNotDefinedForAllRetainingTrailingSpace() {
        rewriteRun(
          pomXml(POM),
          text(
            """
              # This is a comment.
              *       @global-owner1 @global-owner2
              *.js    @js-owner #This is an inline comment.
              /build/logs/ @doctocat
                            
              """,
            """
              # This is a comment.
              *       @jenkinsci/sample-plugin-developers
              *       @global-owner1 @global-owner2
              *.js    @js-owner #This is an inline comment.
              /build/logs/ @doctocat
                            
              """,
            s -> s.path(".github/CODEOWNERS").noTrim()
          )
        );
    }

    @Test
    void shouldAddLineIfTeamNotDefinedForAllRetaining() {
        rewriteRun(
          pomXml(POM),
          text(
            """
              # This is a comment.
              *       @global-owner1 @global-owner2
              *.js    @js-owner #This is an inline comment.
              /build/logs/ @doctocat
              """,
            """
              # This is a comment.
              *       @jenkinsci/sample-plugin-developers
              *       @global-owner1 @global-owner2
              *.js    @js-owner #This is an inline comment.
              /build/logs/ @doctocat
              """,
            s -> s.path(".github/CODEOWNERS").noTrim()
          )
        );
    }

    @Test
    void shouldHandleMultiModule() {
        rewriteRun(
          mavenProject("sample-parent",
            pomXml("""
              <project>
                  <groupId>org.example</groupId>
                  <artifactId>sample-parent</artifactId>
                  <version>0.1</version>
                  <packaging>pom</packaging>
                  <modules>
                      <module>plugin</module>
                      <module>different-plugin</module>
                  </modules>
              </project>
              """),
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
                    <repositories>
                        <repository>
                            <id>repo.jenkins-ci.org</id>
                            <url>https://repo.jenkins-ci.org/public/</url>
                        </repository>
                    </repositories>
                </project>
                """)),
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
                    <repositories>
                        <repository>
                            <id>repo.jenkins-ci.org</id>
                            <url>https://repo.jenkins-ci.org/public/</url>
                        </repository>
                    </repositories>
                </project>
                """))),
          text(
            null,
            """
              * @jenkinsci/sample-plugin-developers
              """,
            s -> s.path(".github/CODEOWNERS").noTrim()
          ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "* @jenkinsci/sample-plugin-developers",
      "\n* @jenkinsci/sample-plugin-developers ",
      "\n* @jenkinsci/sample-plugin-developers\n",
    })
    void shouldNoOpIfTeamAlreadyDefinedForAll(String content) {
        rewriteRun(
          pomXml(POM),
          text(
            content,
            s -> s.path(".github/CODEOWNERS").noTrim()
          )
        );
    }

    @Test
    void shouldNoOpIfInvalidTeamGenerated() {
        rewriteRun(
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                  </parent>
                  <artifactId>tool-labels-plugin</artifactId>
                  <version>0.1</version>
                  <repositories>
                      <repository>
                          <id>repo.jenkins-ci.org</id>
                          <url>https://repo.jenkins-ci.org/public/</url>
                      </repository>
                  </repositories>
              </project>
              """),
          text(
            "* @global-owner1",
            s -> s.path(".github/CODEOWNERS").noTrim()
          )
        );
    }

    @Test
    void shouldNoOpIfInvalidTeamGeneratedAndCodeownersFileAbsent() {
        rewriteRun(
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.86</version>
                  </parent>
                  <artifactId>tool-labels-plugin</artifactId>
                  <version>0.1</version>
                  <repositories>
                      <repository>
                          <id>repo.jenkins-ci.org</id>
                          <url>https://repo.jenkins-ci.org/public/</url>
                      </repository>
                  </repositories>
              </project>
              """)
        );
    }

    @Test
    void shouldNotModifyNonCodeowners() {
        rewriteRun(
          pomXml(POM),
          text("*.iml",
            s -> s.path(".gitignore")),
          text(
            "* @jenkinsci/sample-plugin-developers",
            s -> s.path(".github/CODEOWNERS").noTrim()
          )
        );
    }
}
