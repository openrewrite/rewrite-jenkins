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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.text.CreateTextFile;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextParser;
import org.openrewrite.text.PlainTextVisitor;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Creates the src/main/resources/index.jelly with the project's description
 * if it doesn't exist.
 */
public class CreateIndexJelly extends ScanningRecipe<CreateIndexJelly.Scanned> {

    @Override
    public String getDisplayName() {
        return "Create `index.jelly` if it doesn't exist";
    }

    @Override
    public String getDescription() {
        return "Jenkins tooling [requires](https://github.com/jenkinsci/maven-hpi-plugin/pull/302) " +
                "`src/main/resources/index.jelly` exists with a description.";
    }

    @Override
    public Scanned getInitialValue(ExecutionContext ctx) {
        return new Scanned();
    }

    @Override
    public Collection<PlainText> generate(Scanned acc, ExecutionContext ctx) {
        if (acc.needed()) {
            return new PlainTextParser().parse(acc.contents())
                    .map(brandNewFile -> (PlainText) brandNewFile.withSourcePath(acc.indexJellyPath))
                    .collect(Collectors.toList());
        }
        return emptyList();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Scanned acc) {
        return new MavenIsoVisitor<ExecutionContext>() {
            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext executionContext) {
                Xml.Document d = super.visitDocument(document, executionContext);
                acc.isJenkinsPlugin = Jenkins.isJenkinsPluginPom(d) != null;
                acc.indexJellyPath = d.getSourcePath().resolve("../src/main/resources/index.jelly").normalize();
                return d;
            }

            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                Xml.Tag t = super.visitTag(tag, executionContext);
                if ("description".equals(t.getName())) {
                    acc.description = t.getValue().orElse("");
                } else if ("artifactId".equals(t.getName()) && !isManagedDependencyTag() && !isDependencyTag()) {
                    acc.artifactId = t.getValue().orElseThrow(() -> new IllegalStateException("Expected to find an artifact id"));
                }
                return t;
            }
        };
    }

    static class Scanned {
        boolean isJenkinsPlugin;
        String description = "";
        String artifactId;
        Path indexJellyPath = Paths.get("src/main/resources/index.jelly");

        boolean needed() {
            return isJenkinsPlugin && Files.notExists(indexJellyPath);
        }
        
        String contents() {
            String desc = description.isEmpty() ? artifactId : description;
            return "<?jelly escape-by-default='true'?>\n" +
                    "<div>\n" +
                    desc + "\n" +
                    "</div>\n";
        }
    }
}
