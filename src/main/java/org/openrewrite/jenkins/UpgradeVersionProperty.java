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
package org.openrewrite.jenkins;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.semver.Semver;
import org.openrewrite.semver.VersionComparator;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

import java.util.Collections;
import java.util.Optional;

/**
 * Updates the version property unless it is already greater than minimumVersion
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class UpgradeVersionProperty extends Recipe {
    @Option(displayName = "Key",
            description = "The name of the property key to change.",
            example = "jenkins.version")
    String key;

    @Option(displayName = "Minimum Version",
            description = "Value to apply to the matching property if < this.",
            example = "2.375.1")
    String minimumVersion;

    @Override
    public String getDisplayName() {
        return "Upgrade property's value to version";
    }

    @Override
    public String getDescription() {
        return "If the current value is < given version, upgrade it.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        VersionComparator versionComparator = Semver.validate(minimumVersion, null).getValue();
        assert versionComparator != null;
        return Preconditions.check(new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitDocument(Xml.Document document, ExecutionContext ctx) {
                String value = getResolutionResult().getPom().getProperties().get(key);
                if (value == null) {
                    return document;
                }
                if (value.contains("${jenkins.baseline}")) {
                    value = getResolutionResult().getPom().getProperties().get("jenkins.baseline");
                }
                Optional<String> upgrade = versionComparator.upgrade(value, Collections.singleton(minimumVersion));
                if (!upgrade.isPresent()) {
                    return document;
                }
                return SearchResult.found(document);
            }
        }, new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext ctx) {
                Xml.Tag t = (Tag) super.visitTag(tag, ctx);
                if (!isPropertyTag()) {
                    return t;
                }
                // Change the baseline
                if (t.getName().equals("jenkins.baseline")) {
                    String minimumBaseline = minimumVersion.substring(0, minimumVersion.lastIndexOf('.'));
                    doAfterVisit(new ChangeTagValueVisitor<>(t, minimumBaseline));
                    doAfterVisit(new AddPluginsBom().getVisitor());
                    return t;
                }
                if (!t.getName().equals(key)) {
                    return t;
                }
                if (!t.getValue().isPresent()) {
                    return t;
                }
                String newValue = minimumVersion;
                if (t.getValue().get().contains("${jenkins.baseline}")) {
                    newValue = "${jenkins.baseline}." + minimumVersion.substring(minimumVersion.lastIndexOf('.') + 1);
                }
                doAfterVisit(new ChangeTagValueVisitor<>(t, newValue));
                doAfterVisit(new AddPluginsBom().getVisitor());
                return t;
            }
        });
    }
}
