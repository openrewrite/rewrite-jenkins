/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.jenkins.github;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;

class ArtifactIdTeamNameGeneratorTest {
    private final ArtifactIdTeamNameGenerator generator = new ArtifactIdTeamNameGenerator();

    @ParameterizedTest
    @CsvSource({
      "commons-text-api,@jenkinsci/commons-text-api-plugin-developers",
      "stashNotifier,@jenkinsci/stashnotifier-plugin-developers",
      "aws-java-sdk-parent,@jenkinsci/aws-java-sdk-plugin-developers",
      "warnings-ng-parent,@jenkinsci/warnings-ng-plugin-developers",
      "build-user-vars-plugin,@jenkinsci/build-user-vars-plugin-developers",
      "project-stats-plugin,@jenkinsci/project-stats-plugin-developers",
      "plugin-usage-plugin,@jenkinsci/plugin-usage-plugin-developers",
      "build-keeper-plugin,@jenkinsci/build-keeper-plugin-developers",
    })
    void shouldGenerateExpectedTeamName(String artifactId, String expected) {
        String actual = generator.generate(new TeamNameInput(artifactId));
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/updatecenter-artifactIds.txt")
    void shouldGenerateValidTeamName(String artifactId) {
        String actual = generator.generate(new TeamNameInput(artifactId));
        if (!actual.isEmpty()) {
            boolean exists = exists(actual);
            assertThat(exists).as("artifactId %s's team name is %s", artifactId, actual).isTrue();
        }
    }

    private static boolean exists(String team) {
        try (InputStream is = ArtifactIdTeamNameGeneratorTest.class.getResourceAsStream("/plugin-developers-teams.txt")) {
            assertThat(is).isNotNull();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                return br.lines()
                  .filter(s -> !s.isBlank())
                  .map(String::trim)
                  .map(s -> "@jenkinsci/" + s)
                  .anyMatch(s -> s.equals(team));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
