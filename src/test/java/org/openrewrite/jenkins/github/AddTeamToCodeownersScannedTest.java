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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openrewrite.jenkins.github.AddTeamToCodeowners.Scanned;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AddTeamToCodeownersScannedTest {
    @Mock
    private TeamNameGenerator<TeamNameInput> generator;
    @Mock
    private TeamNameValidator validator;

    @Test
    void shouldBeInvalid() {
        String teamName = "abc";
        given(generator.generate(any())).willReturn(teamName);
        given(validator.isValid(teamName)).willReturn(false);
        Scanned scanned = new Scanned(generator, validator);
        scanned.artifactId = teamName;

        boolean actual = scanned.hasValidTeamName();

        assertThat(actual).isFalse();
    }


    @Test
    void shouldBeValid() {
        String teamName = "abc";
        given(generator.generate(any())).willReturn(teamName);
        given(validator.isValid(teamName)).willReturn(true);
        Scanned scanned = new Scanned(generator, validator);
        scanned.artifactId = teamName;

        boolean actual = scanned.hasValidTeamName();

        assertThat(actual).isTrue();
    }
}
