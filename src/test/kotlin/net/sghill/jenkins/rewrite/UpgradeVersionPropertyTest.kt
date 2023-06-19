package net.sghill.jenkins.rewrite

import org.junit.jupiter.api.Test
import org.openrewrite.maven.Assertions.pomXml
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class UpgradeVersionPropertyTest : RewriteTest {

    override fun defaults(spec: RecipeSpec) {
        spec.recipe(UpgradeVersionProperty("jenkins.version", "2.364.1"))
    }
    
    @Test
    fun upgrades() = rewriteRun(
        pomXml(
            """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.40</version>
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
            """,
            """
                <project>
                    <parent>
                        <groupId>org.jenkins-ci.plugins</groupId>
                        <artifactId>plugin</artifactId>
                        <version>4.40</version>
                        <relativePath/>
                    </parent>
                    <artifactId>example-plugin</artifactId>
                    <version>0.8-SNAPSHOT</version>
                    <properties>
                        <jenkins.version>2.364.1</jenkins.version>
                    </properties>
                    <repositories>
                        <repository>
                            <id>repo.jenkins-ci.org</id>
                            <url>http://repo.jenkins-ci.org/public/</url>
                        </repository>
                    </repositories>
                </project>
            """
        )
    )

    @Test
    fun doesNotDowngrade() = rewriteRun(
            pomXml(
                    """
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>4.67</version>
                                <relativePath/>
                            </parent>
                            <artifactId>example-plugin</artifactId>
                            <version>0.8-SNAPSHOT</version>
                            <properties>
                                <jenkins.version>2.375.1</jenkins.version>
                            </properties>
                            <repositories>
                                <repository>
                                    <id>repo.jenkins-ci.org</id>
                                    <url>http://repo.jenkins-ci.org/public/</url>
                                </repository>
                            </repositories>
                        </project>
                    """            )
    )
}
