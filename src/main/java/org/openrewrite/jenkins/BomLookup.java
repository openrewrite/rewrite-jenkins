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

import com.google.common.io.Resources;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A registry of versions that are supplied by the bom.
 * Versions supplied by the pom can be removed from the dependency declarations.
 */
@Getter
class BomLookup {
    private final Set<String> artifactsInBom = new HashSet<>();

    /**
     * Checks if the bom contains a version for the dependency.
     * @param groupId dependency's groupId
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
        try {
            URL resource = Resources.getResource("jenkins-plugins-bom-lookup.txt");
            List<String> groupArtifacts = Resources.readLines(resource, StandardCharsets.UTF_8);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
