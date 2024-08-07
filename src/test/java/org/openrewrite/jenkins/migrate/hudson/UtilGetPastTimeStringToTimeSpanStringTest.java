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
package org.openrewrite.jenkins.migrate.hudson;

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UtilGetPastTimeStringToTimeSpanStringTest implements RewriteTest {

    @Language("java")
    // language=java
    private final String hudsonUtil = """
      package hudson;

      public class Util {
          public static String getTimeSpanString(long duration) {
                  return "anything";
          }

          @Deprecated
          public static String getPastTimeString(long duration) {
              return getTimeSpanString(duration);
          }
      }
      """;

    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion().dependsOn(hudsonUtil));
        spec.recipeFromResource("/META-INF/rewrite/hudson-migrations.yml", "org.openrewrite.jenkins.migrate.hudson.UtilGetPastTimeStringToGetTimeSpanString");
    }

    @DocumentExample
    @Test
    void shouldReplaceFullyQualifiedMethodCall() {
        // language=java
        rewriteRun(java(
          """
            package org.example;

            class MyConsumer {
                String format(long timestamp) {
                    return hudson.Util.getPastTimeString(timestamp);
                }
            }
            """,
          """
            package org.example;

            class MyConsumer {
                String format(long timestamp) {
                    return hudson.Util.getTimeSpanString(timestamp);
                }
            }
            """
        ));
    }

    @Test
    void shouldReplaceImportedMethodCall() {
        // language=java
        rewriteRun(java(
          """
            package org.example;

            import hudson.Util;

            class MyConsumer {
                String format(long timestamp) {
                    return Util.getPastTimeString(timestamp);
                }
            }
            """,
          """
            package org.example;

            import hudson.Util;

            class MyConsumer {
                String format(long timestamp) {
                    return Util.getTimeSpanString(timestamp);
                }
            }
            """
        ));
    }

    @Test
    void shouldReplaceStaticImportedMethodCall() {
        // language=java
        rewriteRun(java(
          """
            package org.example;

            import static hudson.Util.getPastTimeString;

            class MyConsumer {
                String format(long timestamp) {
                    return getPastTimeString(timestamp);
                }
            }
            """,
          """
            package org.example;

            import static hudson.Util.getTimeSpanString;

            class MyConsumer {
                String format(long timestamp) {
                    return getTimeSpanString(timestamp);
                }
            }
            """
        ));
    }
}
