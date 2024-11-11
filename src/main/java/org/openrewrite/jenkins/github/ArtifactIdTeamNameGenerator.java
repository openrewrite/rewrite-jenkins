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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

class ArtifactIdTeamNameGenerator implements TeamNameGenerator<TeamNameInput> {
    private static final String EXCLUDE = "EXCLUDE";
    private static final String ORG = "@jenkinsci/";
    private final Map<String, String> artifactIdAdvice = loadAdvice();

    @Override
    public String generate(TeamNameInput input) {
        String artifactId = input.getArtifactId();
        String advice = artifactIdAdvice.get(artifactId);
        if (EXCLUDE.equalsIgnoreCase(advice)) {
            return "";
        }
        if (advice != null) {
            return ORG + advice;
        }
        String withoutParent = artifactId;
        if (artifactId.endsWith("-parent") || artifactId.endsWith("-plugin")) {
            withoutParent = artifactId.substring(0, artifactId.lastIndexOf('-'));
        }
        return (ORG + (withoutParent + "-plugin-developers")).toLowerCase(Locale.ROOT);
    }

    private static Map<String, String> loadAdvice() {
        Properties p = new Properties();
        try (InputStream is = ArtifactIdTeamNameGenerator.class.getResourceAsStream("teams.properties")) {
            Map<String, String> o = new HashMap<>();
            p.load(is);
            for (Map.Entry<Object, Object> entry : p.entrySet()) {
                o.put((String) entry.getKey(), (String) entry.getValue());
            }
            return o;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
