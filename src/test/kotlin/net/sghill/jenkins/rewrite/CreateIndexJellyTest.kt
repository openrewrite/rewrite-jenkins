package net.sghill.jenkins.rewrite

import org.junit.jupiter.api.Test
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class CreateIndexJellyTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(CreateIndexJelly())
    }

    @Test
    fun indexJellyAlreadyExists() = rewriteRun(
        other("peanut butter and...") {
            spec -> spec.path("src/main/resources/index.jelly")
        }
    )

    @Test
    fun createIndexJelly() = rewriteRun(
        mavenProject("plugin",
            pomXml("""
                <project>
                    <parent>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>plugin</artifactId>
                        <version>4.40</version>
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
            """),
            srcMainResources(
                text(null, "TODO insert contents here") {
                    spec -> spec.path("index.jelly")
                }
            )
        )
    )
}
