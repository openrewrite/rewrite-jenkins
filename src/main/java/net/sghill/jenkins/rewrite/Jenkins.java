package net.sghill.jenkins.rewrite;

import org.openrewrite.SourceFile;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.tree.MavenResolutionResult;

public class Jenkins {
    @Nullable
    public static String isJenkinsPluginPom(SourceFile sourceFile) {
        return sourceFile.getMarkers()
                .findFirst(MavenResolutionResult.class)
                .map(mavenResolution -> mavenResolution.getPom().getManagedVersion("org.jenkins-ci.main",
                            "jenkins-core", null, null))
                .orElse(null);
    }
}
