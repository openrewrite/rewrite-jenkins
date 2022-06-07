package net.sghill.jenkins.rewrite

import org.junit.jupiter.api.Test
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest

class ReplaceLibrariesWithApiPluginTest : RewriteTest {

    override fun defaults(spec: RecipeSpec) {
        spec.recipe(
            ReplaceLibrariesWithApiPlugin(
                "io.jenkins.plugins", "commons-text-api", "1.9-5.v7ea_44fe6061c", setOf(
                    ReplaceLibrariesWithApiPlugin.Library("org.apache.commons", "commons-text")
                )
            )
        )
    }

    @Test
    fun yamlDefinition() = rewriteRun(
        { spec ->
            spec.recipe(
                "/replace-libraries-with-api-plugin.yml",
                "net.sghill.jenkins.rewrite.CommonsLang3ToApiPlugin"
            )
        },
        pomXml(
            """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.40</version>
                    <relativePath />
                </parent>
            
                <properties>
                    <jenkins.version>2.289.1</jenkins.version>
                </properties>
            
                <dependencies>
                    <dependency>
                        <groupId>org.apache.commons</groupId>
                        <artifactId>commons-text</artifactId>
                        <version>1.9</version>
                    </dependency>
                </dependencies>
            
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
                    <relativePath />
                </parent>
            
                <properties>
                    <jenkins.version>2.289.1</jenkins.version>
                </properties>
            
                <dependencies>
                    <dependency>
                        <groupId>io.jenkins.plugins</groupId>
                        <artifactId>commons-text-api</artifactId>
                        <version>1.9-5.v7ea_44fe6061c</version>
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
        )
    )

    @Test
    fun replacesDirectDependencyWithApiPlugin() = rewriteRun(
        pomXml(
            """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.40</version>
                    <relativePath />
                </parent>
            
                <properties>
                    <jenkins.version>2.289.1</jenkins.version>
                </properties>
            
                <dependencies>
                    <dependency>
                        <groupId>org.apache.commons</groupId>
                        <artifactId>commons-text</artifactId>
                        <version>1.9</version>
                    </dependency>
                </dependencies>
            
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
                    <relativePath />
                </parent>
            
                <properties>
                    <jenkins.version>2.289.1</jenkins.version>
                </properties>
            
                <dependencies>
                    <dependency>
                        <groupId>io.jenkins.plugins</groupId>
                        <artifactId>commons-text-api</artifactId>
                        <version>1.9-5.v7ea_44fe6061c</version>
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
        )
    )

    @Test
    fun excludesTransitivesFromBundledLibrary() = rewriteRun(
        pomXml(
            """
            <project>
                <parent>
                    <groupId>org.jenkins-ci.plugins</groupId>
                    <artifactId>plugin</artifactId>
                    <version>4.40</version>
                    <relativePath />
                </parent>
            
                <properties>
                    <jenkins.version>2.289.1</jenkins.version>
                </properties>
            
                <dependencies>
                    <dependency>
                        <groupId>org.apache.turbine</groupId>
                        <artifactId>turbine</artifactId>
                        <version>5.1</version>
                    </dependency>
                </dependencies>
            
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
                    <relativePath />
                </parent>
            
                <properties>
                    <jenkins.version>2.289.1</jenkins.version>
                </properties>
            
                <dependencies>
                    <dependency>
                        <groupId>io.jenkins.plugins</groupId>
                        <artifactId>commons-text-api</artifactId>
                        <version>1.9-5.v7ea_44fe6061c</version>
                    </dependency>
                    <dependency>
                        <groupId>org.apache.turbine</groupId>
                        <artifactId>turbine</artifactId>
                        <version>5.1</version>
                        <exclusions>
                            <exclusion>
                                <groupId>org.apache.commons</groupId>
                                <artifactId>commons-text</artifactId>
                            </exclusion>
                        </exclusions>
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
        )
    )
}
