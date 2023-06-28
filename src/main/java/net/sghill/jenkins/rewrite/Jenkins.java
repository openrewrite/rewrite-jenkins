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
package net.sghill.jenkins.rewrite;

import org.openrewrite.SourceFile;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.tree.MavenResolutionResult;

/**
 * Utility class
 */
public class Jenkins {
    /**
     * Determines if this is a Jenkins Plugin Pom by checking for a managed version
     * of org.jenkins-ci.main:jenkins-core.
     * @param sourceFile POM
     * @return jenkins-core's version if managed, otherwise null
     */
    @Nullable
    public static String isJenkinsPluginPom(SourceFile sourceFile) {
        return sourceFile.getMarkers()
                .findFirst(MavenResolutionResult.class)
                .map(mavenResolution -> mavenResolution.getPom().getManagedVersion("org.jenkins-ci.main",
                            "jenkins-core", null, null))
                .orElse(null);
    }
}
