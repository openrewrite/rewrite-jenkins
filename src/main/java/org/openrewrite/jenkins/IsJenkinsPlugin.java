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
import org.openrewrite.semver.Semver;
import org.openrewrite.semver.VersionComparator;
import org.openrewrite.xml.XmlVisitor;
import org.openrewrite.xml.tree.Xml;

/**
 * Determines if this project is a Jenkins plugin by checking if the POM
 * has a managed version of jenkins-core.
 */
@Value
@EqualsAndHashCode(callSuper = false)
public class IsJenkinsPlugin extends Recipe {
    @Option(displayName = "Jenkins version",
            description = "The value of the `<jenkins.version>` property.",
            example = "[1,)")
    String version;

    @Override
    public String getDisplayName() {
        return "Is the project a Jenkins plugin?";
    }

    @Override
    public String getDescription() {
        return "Checks if the project is a Jenkins plugin by the presence of a managed version of jenkins-core";
    }

    @Override
    public Validated<Object> validate() {
        Validated<Object> validated = super.validate();
        //noinspection ConstantConditions
        if (version != null) {
            validated = validated.or(Semver.validate(version, null));
        }
        return validated;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        Validated<VersionComparator> versionValidation = Semver.validate(version, null);
        if (versionValidation.isValid()) {
            VersionComparator versionComparator = versionValidation.getValue();
            if (versionComparator != null) {
                return new XmlVisitor<ExecutionContext>() {
                    @Override
                    public Xml visitDocument(Xml.Document document, ExecutionContext ctx) {
                        String jenkinsVersion = Jenkins.isJenkinsPluginPom(document);
                        if (jenkinsVersion != null && versionComparator.isValid(null, jenkinsVersion) &&
                                !document.getMarkers().findFirst(SearchResult.class).isPresent()) {
                            return SearchResult.found(document, jenkinsVersion);
                        }
                        return document;
                    }
                };
            }
        }

        return TreeVisitor.noop();
    }
}
