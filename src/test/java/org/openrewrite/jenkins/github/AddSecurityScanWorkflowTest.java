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
package org.openrewrite.jenkins.github;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.openrewrite.yaml.Assertions.yaml;

class AddSecurityScanWorkflowTest implements RewriteTest {

    @Test
    void shouldNoOp() {
        // language=yaml
        rewriteRun(
                s -> s.recipe(new AddSecurityScanWorkflow(null, null, null)),
                yaml("""
                                # More information about the Jenkins security scan can be found at the developer docs: https://www.jenkins.io/redirect/jenkins-security-scan/
                                              
                                name: Jenkins Security Scan
                                on:
                                  push:
                                    branches:
                                      - "master"
                                      - "main"
                                  pull_request:
                                    types: [ opened, synchronize, reopened ]
                                  workflow_dispatch:
                                              
                                permissions:
                                  security-events: write
                                  contents: read
                                  actions: read
                                              
                                jobs:
                                  security-scan:
                                    uses: jenkins-infra/jenkins-security-scan/.github/workflows/jenkins-security-scan.yaml@v2
                                    with:
                                      java-cache: 'maven' # Optionally enable use of a build dependency cache. Specify 'maven' or 'gradle' as appropriate.
                                      java-version: 11 # What version of Java to set up for the build.
                                """.stripIndent(),
                        s -> s.path(".github/workflows/jenkins-security-scan.yml")
                )
        );
    }

    @Test
    @DocumentExample
    void shouldAddFileIfMissing() {
        // language=yaml
        rewriteRun(
                s -> s.recipe(new AddSecurityScanWorkflow(null, null, null)),
                yaml(null,
                        """
                                # More information about the Jenkins security scan can be found at the developer docs: https://www.jenkins.io/redirect/jenkins-security-scan/
                                              
                                name: Jenkins Security Scan
                                on:
                                  push:
                                    branches:
                                      - "master"
                                      - "main"
                                  pull_request:
                                    types: [ opened, synchronize, reopened ]
                                  workflow_dispatch:
                                              
                                permissions:
                                  security-events: write
                                  contents: read
                                  actions: read
                                              
                                jobs:
                                  security-scan:
                                    uses: jenkins-infra/jenkins-security-scan/.github/workflows/jenkins-security-scan.yaml@v2
                                    with:
                                      java-cache: 'maven' # Optionally enable use of a build dependency cache. Specify 'maven' or 'gradle' as appropriate.
                                      java-version: 11 # What version of Java to set up for the build.
                                """.stripIndent(),
                        s -> s.path(".github/workflows/jenkins-security-scan.yml")
                )
        );
    }

    @Test
    void shouldAddFileWithDeclaredBranch() {
        rewriteRun(
                s -> s.recipe(new AddSecurityScanWorkflow(List.of("release/1.x"), null, null)),
                yaml(null,
                        // language=yaml
                        """
                                # More information about the Jenkins security scan can be found at the developer docs: https://www.jenkins.io/redirect/jenkins-security-scan/
                                              
                                name: Jenkins Security Scan
                                on:
                                  push:
                                    branches:
                                      - "release/1.x"
                                  pull_request:
                                    types: [ opened, synchronize, reopened ]
                                  workflow_dispatch:
                                              
                                permissions:
                                  security-events: write
                                  contents: read
                                  actions: read
                                              
                                jobs:
                                  security-scan:
                                    uses: jenkins-infra/jenkins-security-scan/.github/workflows/jenkins-security-scan.yaml@v2
                                    with:
                                      java-cache: 'maven' # Optionally enable use of a build dependency cache. Specify 'maven' or 'gradle' as appropriate.
                                      java-version: 11 # What version of Java to set up for the build.
                                """.stripIndent(),
                        s -> s.path(".github/workflows/jenkins-security-scan.yml")
                )
        );
    }

    @Test
    void shouldAddFileWithDeclaredJavaVersion() {
        rewriteRun(
                s -> s.recipe(new AddSecurityScanWorkflow(null, 17, null)),
                yaml(null,
                        // language=yaml
                        """
                                # More information about the Jenkins security scan can be found at the developer docs: https://www.jenkins.io/redirect/jenkins-security-scan/
                                              
                                name: Jenkins Security Scan
                                on:
                                  push:
                                    branches:
                                      - "master"
                                      - "main"
                                  pull_request:
                                    types: [ opened, synchronize, reopened ]
                                  workflow_dispatch:
                                              
                                permissions:
                                  security-events: write
                                  contents: read
                                  actions: read
                                              
                                jobs:
                                  security-scan:
                                    uses: jenkins-infra/jenkins-security-scan/.github/workflows/jenkins-security-scan.yaml@v2
                                    with:
                                      java-cache: 'maven' # Optionally enable use of a build dependency cache. Specify 'maven' or 'gradle' as appropriate.
                                      java-version: 17 # What version of Java to set up for the build.
                                """.stripIndent(),
                        s -> s.path(".github/workflows/jenkins-security-scan.yml")
                )
        );
    }

    @Test
    void shouldAddFileWithDeclaredBuildTool() {
        rewriteRun(
                s -> s.recipe(new AddSecurityScanWorkflow(null, null, "gradle")),
                yaml(null,
                        // language=yaml
                        """
                                # More information about the Jenkins security scan can be found at the developer docs: https://www.jenkins.io/redirect/jenkins-security-scan/
                                              
                                name: Jenkins Security Scan
                                on:
                                  push:
                                    branches:
                                      - "master"
                                      - "main"
                                  pull_request:
                                    types: [ opened, synchronize, reopened ]
                                  workflow_dispatch:
                                              
                                permissions:
                                  security-events: write
                                  contents: read
                                  actions: read
                                              
                                jobs:
                                  security-scan:
                                    uses: jenkins-infra/jenkins-security-scan/.github/workflows/jenkins-security-scan.yaml@v2
                                    with:
                                      java-cache: 'gradle' # Optionally enable use of a build dependency cache. Specify 'maven' or 'gradle' as appropriate.
                                      java-version: 11 # What version of Java to set up for the build.
                                """.stripIndent(),
                        s -> s.path(".github/workflows/jenkins-security-scan.yml")
                )
        );
    }

    @Test
    void shouldChangeFile() {
        rewriteRun(
                s -> s.recipe(new AddSecurityScanWorkflow(List.of("release/1.x"), 21, "gradle")),
                // language=yaml
                yaml("""
                                # More information about the Jenkins security scan can be found at the developer docs: https://www.jenkins.io/redirect/jenkins-security-scan/
                                              
                                name: Jenkins Security Scan
                                on:
                                  push:
                                    branches:
                                      - "master"
                                      - "main"
                                  pull_request:
                                    types: [ opened, synchronize, reopened ]
                                  workflow_dispatch:
                                              
                                permissions:
                                  security-events: write
                                  contents: read
                                  actions: read
                                              
                                jobs:
                                  security-scan:
                                    uses: jenkins-infra/jenkins-security-scan/.github/workflows/jenkins-security-scan.yaml@v2
                                    with:
                                      java-cache: 'maven' # Optionally enable use of a build dependency cache. Specify 'maven' or 'gradle' as appropriate.
                                      java-version: 17 # What version of Java to set up for the build.
                                """.stripIndent(),
                        """
                                # More information about the Jenkins security scan can be found at the developer docs: https://www.jenkins.io/redirect/jenkins-security-scan/
                                              
                                name: Jenkins Security Scan
                                on:
                                  push:
                                    branches:
                                      - "release/1.x"
                                  pull_request:
                                    types: [ opened, synchronize, reopened ]
                                  workflow_dispatch:
                                              
                                permissions:
                                  security-events: write
                                  contents: read
                                  actions: read
                                              
                                jobs:
                                  security-scan:
                                    uses: jenkins-infra/jenkins-security-scan/.github/workflows/jenkins-security-scan.yaml@v2
                                    with:
                                      java-cache: 'gradle' # Optionally enable use of a build dependency cache. Specify 'maven' or 'gradle' as appropriate.
                                      java-version: 21 # What version of Java to set up for the build.
                                """.stripIndent(),
                        s -> s.path(".github/workflows/jenkins-security-scan.yml")
                )
        );
    }

}
