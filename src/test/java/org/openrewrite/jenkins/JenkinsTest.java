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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class JenkinsTest {

    /**
     * This is biased toward recency.
     * If we don't recognize the version as a LTS we assume it is very recent and wants the weekly bom.
     */
    @ParameterizedTest
    @MethodSource("versionToBom")
    void shouldGenerateBomNameFromJenkinsVersion(String jenkinsVersion, String bomVersion) {
        String actual = Jenkins.bomNameForJenkinsVersion(jenkinsVersion);

        assertThat(actual).isEqualTo(bomVersion);
    }

    static Stream<Arguments> versionToBom() {
        return Stream.of(
          arguments("2.277.3", "bom-2.277.x"),
          arguments("2.319.1", "bom-2.319.x"),
          arguments("2.361.4", "bom-2.361.x"),
          arguments("2.401.2", "bom-2.401.x"),
          arguments("2.384", "bom-weekly"),
          arguments("2.401", "bom-weekly"),
          arguments("888888-SNAPSHOT", "bom-weekly"),
          arguments("2.379-rc33114.2f90818f6a_35", "bom-weekly")
        );
    }
}
