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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.Recipe;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.groovy.Assertions.groovy;
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
    @DocumentExample
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

    @Test
    @Disabled
    void shouldAddJdk21IfAbsent() {
        rewriteRun(spec -> spec.recipe(new AddBuildPluginConfiguration("linux", "21")),
                groovy("""
                                buildPlugin()
                                """.stripIndent(), """
                                buildPlugin(configurations:[
                                  [ platform: 'linux', jdk: '21' ]
                                ])
                                """.stripIndent(),
                        spec -> spec.path("Jenkinsfile")
                ));
    }

    @Test
    @Disabled
    void shouldRemoveJdk8IfPresent() {
        rewriteRun(spec -> spec.recipe(new RemoveBuildPluginConfiguration("linux", "8")),
                groovy("""
                                buildPlugin(useContainerAgent: true, configurations: [
                                  [ platform: 'linux', jdk: '8' ],
                                  [ platform: 'windows', jdk: '8' ],
                                  [ platform: 'windows', jdk: '11' ],
                                  [ platform: 'linux', jdk: '17' ],
                                ])
                                """.stripIndent(), """
                                buildPlugin(useContainerAgent: true, configurations: [
                                  [ platform: 'windows', jdk: '8' ],
                                  [ platform: 'windows', jdk: '11' ],
                                  [ platform: 'linux', jdk: '17' ],
                                ])
                                """.stripIndent(),
                        spec -> spec.path("Jenkinsfile")
                ));
    }

    @Test
    @Disabled
    void shouldRemoveJdk8FromAllPlatformsIfPresent() {
        rewriteRun(spec -> spec.recipe(new RemoveBuildPluginConfiguration("8")),
                groovy("""
                                buildPlugin(useContainerAgent: true, failFast: false, configurations: [
                                  [ platform: 'linux', jdk: '8' ],
                                  [ platform: 'windows', jdk: '8' ],
                                  [ platform: 'windows', jdk: '11' ],
                                  [ platform: 'linux', jdk: '17' ],
                                ])
                                """.stripIndent(), """
                                buildPlugin(useContainerAgent: true, failFast: false, configurations: [
                                  [ platform: 'windows', jdk: '11' ],
                                  [ platform: 'linux', jdk: '17' ],
                                ])
                                """.stripIndent(),
                        spec -> spec.path("Jenkinsfile")
                ));
    }

    @Test
    @Disabled
    void shouldRemoveJenkinsVersionsLessThan() {
        rewriteRun(spec -> spec.recipe(new RemoveJenkinsVersionsLessThan("2.346.3")),
                groovy("""
                                buildPlugin(jenkinsVersions: [null, '2.60.1', '2.387.3'])
                                """.stripIndent(), """
                                buildPlugin(jenkinsVersions: [null, '2.387.3'])
                                """.stripIndent(),
                        spec -> spec.path("Jenkinsfile")
                ));
    }

    @Test
    @Disabled
    void shouldRemoveKeyIfRemoveJenkinsVersionsLessThanLeavesOnlyNull() {
        rewriteRun(spec -> spec.recipe(new RemoveJenkinsVersionsLessThan("2.346.3")),
                groovy("""
                                buildPlugin(jenkinsVersions: [null, '2.60.1'])
                                """.stripIndent(), """
                                buildPlugin()
                                """.stripIndent(),
                        spec -> spec.path("Jenkinsfile")
                ));
    }

    private static class AddBuildPluginConfiguration extends Recipe {
        public AddBuildPluginConfiguration(String platform, String jdk) {
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private static class RemoveBuildPluginConfiguration extends Recipe {
        public RemoveBuildPluginConfiguration(String jdk) {
        }

        public RemoveBuildPluginConfiguration(String platform, String jdk) {
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }

    private class RemoveJenkinsVersionsLessThan extends Recipe {
        public RemoveJenkinsVersionsLessThan(String version) {
        }

        @Override
        public String getDisplayName() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }
    }
}
