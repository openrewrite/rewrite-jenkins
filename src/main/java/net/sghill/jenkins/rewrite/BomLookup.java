package net.sghill.jenkins.rewrite;

import lombok.Getter;
import org.openrewrite.InMemoryExecutionContext;
import org.openrewrite.internal.PropertyPlaceholderHelper;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.maven.MavenExecutionContextView;
import org.openrewrite.maven.MavenParser;
import org.openrewrite.maven.cache.CompositeMavenPomCache;
import org.openrewrite.maven.cache.InMemoryMavenPomCache;
import org.openrewrite.maven.cache.RocksdbMavenPomCache;
import org.openrewrite.maven.tree.GroupArtifact;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.Paths;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A registry of versions that are supplied by the bom.
 * Versions supplied by the pom can be removed from the dependency declarations.
 */
@Getter
public class BomLookup {
    private final Set<GroupArtifact> groupArtifacts;

    /**
     * Creates a BomLookup with the default bom version and parent plugin version, writing to ~/.rewrite-cache
     */
    public BomLookup() {
        this("bom-2.303.x:1409.v7659b_c072f18", "4.40");
    }

    /**
     * Creates a BomLookup, writing to ~/.rewrite-cache
     * @param bomArtifactVersion bom version to index
     * @param parentVersion parent plugin version
     */
    public BomLookup(String bomArtifactVersion, String parentVersion) {
        PropertyPlaceholderHelper placeholderHelper = new PropertyPlaceholderHelper("${", "}", null);
        Properties props = new Properties();
        String[] bomAV = bomArtifactVersion.split(":");
        props.put("bomArtifact", bomAV[0]);
        props.put("bomVersion", bomAV[1]);
        props.put("parentVersion", parentVersion);

        MavenExecutionContextView ctx = MavenExecutionContextView.view(new InMemoryExecutionContext());
        ctx.setPomCache(new CompositeMavenPomCache(
                new InMemoryMavenPomCache(),
                new RocksdbMavenPomCache(Paths.get(System.getProperty("user.home")))
        ));

        Xml.Document maven = MavenParser.builder()
                .build()
                .parse(ctx, placeholderHelper.replacePlaceholders(StringUtils.readFully(
                        requireNonNull(BomLookup.class.getResourceAsStream("/jenkins-bom.xml"))
                ), props))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Classpath resource could not be found"));

        MavenResolutionResult resolved = maven.getMarkers().findFirst(MavenResolutionResult.class)
                .orElseThrow(() -> new IllegalStateException("Expected a resolution result for BOM"));

        groupArtifacts = resolved
                .getPom()
                .getDependencyManagement()
                .stream()
                .map(d -> new GroupArtifact(d.getGroupId(), d.getArtifactId()))
                .collect(Collectors.toSet());
    }

    /**
     * Checks if the bom contains a version for the dependency.
     * @param groupId dependency's groupId
     * @param artifactId dependency's artifactId
     * @return true if version can be dropped from dependency
     */
    public boolean inBom(String groupId, String artifactId) {
        return groupArtifacts.contains(new GroupArtifact(groupId, artifactId));
    }
}
