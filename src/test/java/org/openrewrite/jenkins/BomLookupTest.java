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
package org.openrewrite.jenkins;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import static org.assertj.core.api.Assertions.assertThat;

class BomLookupTest {
    private final BomLookup bomLookup = new BomLookup();

    @Test
    void shouldLookupByGroupIdAndArtifactId() {
        // Check two explicitly known artifacts
        assertThat(bomLookup.inBom("io.jenkins.plugins", "theme-manager")).isTrue();
        assertThat(bomLookup.inBom("org.jenkins-ci.plugins", "artifactory")).isFalse();
    }

    @CsvFileSource(resources = "/jenkins-plugins-bom-lookup.txt", delimiter = ':')
    @ParameterizedTest
    void shouldLookupByGroupIdAndArtifactId(String groupId, String artifactId) {
        assertThat(bomLookup.inBom(groupId, artifactId)).isTrue();
    }
}
