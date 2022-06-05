package net.sghill.jenkins.rewrite

import org.junit.jupiter.api.Test
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class ModernizeJenkinsPluginTest : RewriteTest {

    override fun defaults(spec: RecipeSpec) {
        spec
                .recipe("/META-INF/rewrite/rewrite.yml", "net.sghill.jenkins.ModernizePlugin")
    }

    @Test
    fun majorVersionUpgrade() = rewriteRun(
            pomXml(
                    """
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>3.40</version>
                            </parent>
                            <artifactId>permissive-script-security</artifactId>
                            <version>0.8-SNAPSHOT</version>
                            <properties>
                                <java.level>8</java.level>
                                <jenkins.version>2.107.3</jenkins.version>
                            </properties>
                            <repositories>
                                <repository>
                                    <id>repo.jenkins-ci.org</id>
                                    <url>https://repo.jenkins-ci.org/public/</url>
                                </repository>
                            </repositories>
                        </project>
                    """, """
                        <project>
                            <parent>
                                <groupId>org.jenkins-ci.plugins</groupId>
                                <artifactId>plugin</artifactId>
                                <version>4.40</version>
                            </parent>
                            <artifactId>permissive-script-security</artifactId>
                            <version>0.8-SNAPSHOT</version>
                            <properties>
                                <jenkins.version>2.303.3</jenkins.version>
                            </properties>
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