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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactIdTeamNameGeneratorTest {
    private final ArtifactIdTeamNameGenerator generator = new ArtifactIdTeamNameGenerator();

    @ParameterizedTest
    @CsvSource({
            "commons-text-api,@jenkinsci/commons-text-api-plugin-developers",
            "stashNotifier,@jenkinsci/stashnotifier-plugin-developers",
            "aws-java-sdk-parent,@jenkinsci/aws-java-sdk-plugin-developers",
            "warnings-ng-parent,@jenkinsci/warnings-ng-plugin-developers",
    })
    void shouldGenerateExpectedTeamName(String artifactId, String expected) {
        String actual = generator.generate(new TeamNameInput(artifactId));
        assertThat(actual).isEqualTo(expected);
    }
}
