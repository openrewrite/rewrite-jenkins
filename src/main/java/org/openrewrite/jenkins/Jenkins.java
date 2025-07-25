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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openrewrite.SourceFile;
import org.openrewrite.maven.tree.MavenResolutionResult;

import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Utility class
 */
class Jenkins {
    private static final Predicate<String> LTS_PATTERN = Pattern.compile("^\\d\\.(\\d+)\\.\\d$").asPredicate();
    private static final Predicate<String> LTS_BASELINE_PATTERN = Pattern.compile("^\\$\\{jenkins.baseline\\}.\\d$").asPredicate();

    /**
     * Determines if this is a Jenkins Plugin Pom by checking for a managed version
     * of org.jenkins-ci.main:jenkins-core.
     *
     * @param sourceFile POM
     * @return jenkins-core's version if managed, otherwise null
     */
    public static @Nullable String isJenkinsPluginPom(SourceFile sourceFile) {
        return sourceFile.getMarkers()
                .findFirst(MavenResolutionResult.class)
                .map(mavenResolution -> mavenResolution.getPom().getManagedVersion("org.jenkins-ci.main",
                            "jenkins-core", null, null))
                .orElse(null);
    }

    @NonNull
    public static String bomNameForJenkinsVersion(@NonNull String version) {
        if (LTS_PATTERN.test(version) || LTS_BASELINE_PATTERN.test(version)) {
            if (version.startsWith("${jenkins.baseline}")) {
                return "bom-${jenkins.baseline}.x";
            }
            int lastIndex = version.lastIndexOf(".");
            String prefix = version.substring(0, lastIndex);
            return "bom-" + prefix + ".x";
        }
        return "bom-weekly";
    }
}
