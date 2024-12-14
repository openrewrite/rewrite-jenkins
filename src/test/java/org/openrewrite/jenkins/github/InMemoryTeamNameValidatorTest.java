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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryTeamNameValidatorTest {
    private final InMemoryTeamNameValidator validator = new InMemoryTeamNameValidator();

    @Test
    void shouldValidate() {
        String input = "@jenkinsci/log-parser-plugin-developers";
        boolean actual = validator.isValid(input);
        assertThat(actual).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
      "@jenkinsci/-plugin-developers",             // we didn't get anything for the artifactId
    })
    void shouldNotValidate(String input) {
        boolean actual = validator.isValid(input);
        assertThat(actual).isFalse();
    }
}
