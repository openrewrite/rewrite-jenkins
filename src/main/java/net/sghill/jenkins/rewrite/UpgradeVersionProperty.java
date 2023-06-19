package net.sghill.jenkins.rewrite;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.Markers;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.semver.Semver;
import org.openrewrite.semver.VersionComparator;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

import java.util.Collections;
import java.util.Optional;

import static org.openrewrite.Tree.randomId;

/**
 * Updates the version property unless it is already greater than minimumVersion
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class UpgradeVersionProperty extends Recipe {
    @Option(displayName = "Key",
            description = "The name of the property key whose value is to be changed.",
            example = "jenkins.version")
    String key;

    @Option(displayName = "Minimum Version",
            description = "Value to apply to the matching property if currently < this.",
            example = "2.375.1")
    String minimumVersion;

    @Override
    public String getDisplayName() {
        return "Upgrade given property to version if necessary";
    }

    @Override
    public String getDescription() {
        return "If the current value is a version < given version, upgrade it.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        VersionComparator versionComparator = Semver.validate(minimumVersion, null).getValue();
        assert versionComparator != null;
        return Preconditions.check(new MavenVisitor<>() {
            @Override
            public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
                String value = getResolutionResult().getPom().getProperties().get(key);
                if (value == null) {
                    return document;
                }
                Optional<String> upgrade = versionComparator.upgrade(value, Collections.singleton(minimumVersion));
                if (upgrade.isEmpty()) {
                    return document;
                }
                return SearchResult.found(document);
            }
        }, new MavenVisitor<>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext ctx) {
                Xml.Tag t = (Tag) super.visitTag(tag, ctx);
                if (!isPropertyTag()) {
                    return t;
                }
                if (!t.getName().equals(key)) {
                    return t;
                }
                doAfterVisit(new ChangeTagValueVisitor<>(t, minimumVersion));
                return t;
            }
        });
    }
}
