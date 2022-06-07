package net.sghill.jenkins.rewrite

import org.junit.jupiter.api.Test
import org.openrewrite.Recipe
import org.openrewrite.config.Environment
import org.openrewrite.java.JavaParser
import org.openrewrite.java.JavaRecipeTest
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class JavaxAnnotationsToSpotbugsTest : JavaRecipeTest {
    override val parser: JavaParser = JavaParser.fromJavaVersion()
            .classpath("jsr305", "spotbugs-annotations")
            .build()

    override val recipe: Recipe = Environment.builder()
            .scanRuntimeClasspath("net.sghill.jenkins.rewrite")
            .build()
            .activateRecipes("net.sghill.jenkins.rewrite.JavaxAnnotationsToSpotbugs")

    @Test
    fun classNameChange() = assertChanged(
            before = """
                        import javax.annotation.Nonnull;
                        
                        public class A {
                            static @Nonnull String CONSTANT = "A";
                        }
                        """,
            after = """
                        import edu.umd.cs.findbugs.annotations.NonNull;
                        
                        public class A {
                            static @NonNull String CONSTANT = "A";
                        }
                        """
    )
    
    @Test
    fun packageChange() = assertChanged(
            before = """
                        import javax.annotation.CheckForNull;
                        
                        public class A {
                            @CheckForNull
                            public String key() {
                                return null;
                            }
                        }
                        """,
            after = """
                        import edu.umd.cs.findbugs.annotations.CheckForNull;
                        
                        public class A {
                            @CheckForNull
                            public String key() {
                                return null;
                            }
                        }
                        """
    )
}
