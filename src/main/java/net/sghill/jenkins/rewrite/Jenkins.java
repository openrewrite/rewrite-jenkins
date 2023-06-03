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
