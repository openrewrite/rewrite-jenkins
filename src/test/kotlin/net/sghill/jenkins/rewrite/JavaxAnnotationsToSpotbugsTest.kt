package net.sghill.jenkins.rewrite

import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions.java
import org.openrewrite.java.JavaParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class JavaxAnnotationsToSpotbugsTest : RewriteTest {

    override fun defaults(spec: RecipeSpec) {
        spec.parser(JavaParser.fromJavaVersion().classpath("jsr305", "spotbugs-annotations"))
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml", "net.sghill.jenkins.rewrite.JavaxAnnotationsToSpotbugs")
    }

    @Test
    fun classNameChange() = rewriteRun(
        java(
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
        )
    )

    @Test
    fun packageChange() = rewriteRun(
        java(
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
        )
    )

    @Test
    fun orderImports() = rewriteRun(
        java(
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
            import edu.umd.cs.findbugs.annotations.CheckForNull;
            import edu.umd.cs.findbugs.annotations.NonNull;

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
        )
    )
}
