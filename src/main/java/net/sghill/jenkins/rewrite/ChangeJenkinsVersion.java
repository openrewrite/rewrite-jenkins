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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.maven.ChangePropertyValue;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.xml.ChangeTagValueVisitor;
import org.openrewrite.xml.tree.Xml;
import org.openrewrite.xml.tree.Xml.Tag;

/**
 * Atomically updates the Jenkins version and parent pom version
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class ChangeJenkinsVersion extends Recipe {
    String parentPomVersion;
    String jenkinsVersion;

    @Override
    public String getDisplayName() {
        return "Upgrade parent POM and Jenkins version";
    }

    @Override
    public String getDescription() {
        return "This upgrade is atomic so we prevent attempting to resolve a BOM that does not exist yet.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitTag(Tag tag, ExecutionContext executionContext) {
                if (isParentTag()) {
                    Tag version = tag.getChild("version")
                            .orElseThrow(() -> new IllegalStateException("Expected to find a version tag for parent"));
                    return new ChangeTagValueVisitor<>(version, parentPomVersion).visitNonNull(tag, executionContext);
                }
                return super.visitTag(tag, executionContext);
            }

            @Override
            public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
                ChangePropertyValue change = new ChangePropertyValue("jenkins.version", jenkinsVersion, true, null);
                Xml.Document doc = (Xml.Document) change.getVisitor().visitNonNull(document, executionContext);
                return super.visitDocument(doc, executionContext);
            }
        };
    }
}
