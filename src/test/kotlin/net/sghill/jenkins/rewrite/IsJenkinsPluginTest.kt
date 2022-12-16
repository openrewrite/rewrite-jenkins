package net.sghill.jenkins.rewrite

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.openrewrite.maven.Assertions.pomXml
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class IsJenkinsPluginTest : RewriteTest {
    override fun defaults(spec: RecipeSpec) {
        spec.recipe(IsJenkinsPlugin("*"))
    }

    @Test
    @Disabled
    fun isJenkinsPlugin() = rewriteRun(
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
            """,
            """
                <!--~~(2.249)~~>--><project>
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
            """
        )
    )
}
