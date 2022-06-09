package net.sghill.jenkins.rewrite;

import org.openrewrite.SourceFile;
import org.openrewrite.maven.tree.MavenResolutionResult;

public class Jenkins {
    public static boolean isJenkinsPluginPom(SourceFile sourceFile) {
        return sourceFile.getMarkers()
                .findFirst(MavenResolutionResult.class)
                .map(mavenResolution -> mavenResolution.getPom().getManagedVersion("org.jenkins-ci.main",
                            "jenkins-core", null, null) != null)
                .orElse(false);
    }
}
