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

@Getter
public class BomLookup {
    private final Set<GroupArtifact> groupArtifacts;

    public BomLookup() {
        this("bom-2.303.x:1409.v7659b_c072f18", "4.40");
    }

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
                .get(0);

        MavenResolutionResult resolved = maven.getMarkers().findFirst(MavenResolutionResult.class)
                .orElseThrow(() -> new IllegalStateException("Expected a resolution result for BOM"));

        groupArtifacts = resolved
                .getPom()
                .getDependencyManagement()
                .stream()
                .map(d -> new GroupArtifact(d.getGroupId(), d.getArtifactId()))
                .collect(Collectors.toSet());
    }

    public boolean inBom(String groupId, String artifactId) {
        return groupArtifacts.contains(new GroupArtifact(groupId, artifactId));
    }
}
