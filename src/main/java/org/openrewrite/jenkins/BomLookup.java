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

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A registry of versions that are supplied by the bom.
 * Versions supplied by the pom can be removed from the dependency declarations.
 */
@Getter
class BomLookup {
    private final Set<String> artifactsInBom = new HashSet<>();

    /**
     * Checks if the bom contains a version for the dependency.
     *
     * @param groupId    dependency's groupId
     * @param artifactId dependency's artifactId
     * @return true if version can be dropped from dependency
     */
    public boolean inBom(String groupId, String artifactId) {
        if (artifactsInBom.isEmpty()) {
            init();
        }
        return artifactsInBom.contains(groupId + ":" + artifactId);
    }

    private void init() {
        try (InputStream is = BomLookup.class.getResourceAsStream("/jenkins-plugins-bom-lookup.txt")) {
            Objects.requireNonNull(is);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<String> groupArtifacts = br.lines().collect(Collectors.toList());
                for (String groupArtifact : groupArtifacts) {
                    if (groupArtifact == null) {
                        continue;
                    }
                    String tidy = groupArtifact.trim();
                    if (tidy.isEmpty()) {
                        continue;
                    }
                    artifactsInBom.add(tidy);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
