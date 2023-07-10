package org.openrewrite.jenkins;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.maven.Assertions.pomXml;
import static org.openrewrite.test.SourceSpecs.text;

class ModernizeJenkinsfileTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml", "org.openrewrite.jenkins.ModernizeJenkinsfile");
    }

    @Test
    void shouldCreateJenkinsfile() {
        rewriteRun(pomXml(
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
                                """),
                text(null, """
                                buildPlugin(useContainerAgent: true, configurations: [
                                  [ platform: 'linux', jdk: '11' ],
                                  [ platform: 'windows', jdk: '11' ],
                                  [ platform: 'linux', jdk: '17' ],
                                ])""".stripIndent(),
                        spec -> spec.path("Jenkinsfile")));
    }

    @Test
    void shouldUpdateJenkinsfile() {
        rewriteRun(pomXml(
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
                                """),
                text("""
                                buildPlugin()
                                """.stripIndent(), """
                                buildPlugin(useContainerAgent: true, configurations: [
                                  [ platform: 'linux', jdk: '11' ],
                                  [ platform: 'windows', jdk: '11' ],
                                  [ platform: 'linux', jdk: '17' ],
                                ])
                                
                                """.stripIndent(),
                        spec -> spec.path("Jenkinsfile")));
    }
}
