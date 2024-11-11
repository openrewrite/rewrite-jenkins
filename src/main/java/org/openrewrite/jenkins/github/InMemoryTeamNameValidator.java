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

import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a simple stopgap to prevent the same issues from recurring.
 * Ideally we'd have a near real-time view of the actual teams in GitHub.
 * At the moment there are over 2500, so making an API call for each
 * recipe run is likely to run into trouble.
 */
class InMemoryTeamNameValidator implements TeamNameValidator {
    private static final Set<String> BANNED = banned();

    @Override
    public boolean isValid(@Nullable String name) {
        return name != null &&
                !name.isEmpty() &&
                !BANNED.contains(name);
    }

    /**
     * Known calculated values that do not map to actual teams
     */
    private static Set<String> banned() {
        Set<String> banned = new HashSet<>();
        banned.add("@jenkinsci/-plugin-developers");
        return banned;
    }
}
