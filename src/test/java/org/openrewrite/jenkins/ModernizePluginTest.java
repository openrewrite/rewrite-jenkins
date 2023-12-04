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

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.java.Assertions.srcMainJava;
import static org.openrewrite.maven.Assertions.pomXml;

class ModernizePluginTest implements RewriteTest {
    @Language("java")
    private final String javax = """
      package javax.inject;
            
      import java.lang.annotation.Documented;
      import java.lang.annotation.Retention;
      import static java.lang.annotation.RetentionPolicy.RUNTIME;
            
      @Documented
      @Retention(RUNTIME)
      public @interface Singleton {}
      """;

    @Language("java")
    private final String jakarta = """
      package jakarta.inject;
            
      import java.lang.annotation.Documented;
      import java.lang.annotation.Retention;
      import static java.lang.annotation.RetentionPolicy.RUNTIME;
            
      @Documented
      @Retention(RUNTIME)
      public @interface Singleton {}
      """;

    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion().dependsOn(javax, jakarta));
        spec.recipeFromResources("org.openrewrite.jenkins.ModernizePlugin");
    }

    @Test
    void shouldDoTheWorks() {
        rewriteRun(
          //language=xml
          pomXml(
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.75</version>
                      <relativePath/>
                  </parent>
                  <artifactId>example-plugin</artifactId>
                  <version>0.8-SNAPSHOT</version>
                  <properties>
                      <jenkins.version>2.387.3</jenkins.version>
                  </properties>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>io.jenkins.tools.bom</groupId>
                              <artifactId>bom-2.387.x</artifactId>
                              <version>2516.v113cb_3d00317</version>
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
            """
              <project>
                  <parent>
                      <groupId>org.jenkins-ci.plugins</groupId>
                      <artifactId>plugin</artifactId>
                      <version>4.75</version>
                      <relativePath/>
                  </parent>
                  <artifactId>example-plugin</artifactId>
                  <version>0.8-SNAPSHOT</version>
                  <properties>
                      <jenkins.version>2.401.3</jenkins.version>
                  </properties>
                  <dependencyManagement>
                      <dependencies>
                          <dependency>
                              <groupId>io.jenkins.tools.bom</groupId>
                              <artifactId>bom-2.401.x</artifactId>
                              <version>2612.v3d6a_2128c0ef</version>
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
              """
          ),
          srcMainJava(spec -> spec.path("something/Example.java"),
            //language=java
            java(
              """
                package something;
                          
                import javax.inject.Singleton;
                          
                @Singleton
                class Example {
                    int add(int a, int b) { return a + b; }
                }
                """,
              """
                package something;
                          
                import jakarta.inject.Singleton;
                          
                @Singleton
                class Example {
                    int add(int a, int b) { return a + b; }
                }
                """
            )
          )
        );
    }
}
