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
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class JavaxAnnotationsToSpotBugsTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion().classpath("jsr305", "spotbugs-annotations"));
        spec.recipeFromResource("/META-INF/rewrite/jsr-305.yml", "org.openrewrite.jenkins.JavaxAnnotationsToSpotbugs");
    }

    @Test
    void shouldChangeClassName() {
        rewriteRun(java(
          """
            import javax.annotation.Nonnull;
                        
            public class A {
                static @Nonnull String CONSTANT = "A";
            }
            """,
          """
            import edu.umd.cs.findbugs.annotations.NonNull;
                        
            public class A {
                static @NonNull String CONSTANT = "A";
            }
            """
        ));
    }

    @Test
    void shouldChangePackage() {
        rewriteRun(java(
          """
            import javax.annotation.CheckForNull;

            public class A {
                @CheckForNull
                public String key() {
                    return null;
                }
            }
            """,
          """
            import edu.umd.cs.findbugs.annotations.CheckForNull;
                
            public class A {
                @CheckForNull
                public String key() {
                    return null;
                }
            }
            """
        ));
    }

    @Test
    @DocumentExample
    void shouldNotOrderImports() {
        rewriteRun(java(
          """
            import javax.annotation.CheckForNull;
            import javax.annotation.Nonnull;
            import java.util.Objects;
                            
            public class A {
                @CheckForNull
                public String key() {
                    return null;
                }
                            
                public @Nonnull String myMethod(String in) {
                    return Objects.equals(in, "a") ? "yes" : "no";
                }
            }
            """,
          """
            import edu.umd.cs.findbugs.annotations.NonNull;
                            
            import edu.umd.cs.findbugs.annotations.CheckForNull;
            import java.util.Objects;
                            
            public class A {
                @CheckForNull
                public String key() {
                    return null;
                }
                            
                public @NonNull String myMethod(String in) {
                    return Objects.equals(in, "a") ? "yes" : "no";
                }
            }
            """
        ));
    }
}
