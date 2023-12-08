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

import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextParser;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

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
        List<PlainText> generated = new LinkedList<>();
        PlainTextParser parser = new PlainTextParser();
        for (DescribedPlugin plugin : acc.plugins) {
            if (acc.indexJellies.contains(plugin.indexJellyPath)) {
                continue;
            }
            parser.parse(plugin.contents())
                    .map(brandNewFile -> (PlainText) brandNewFile.withSourcePath(Paths.get(plugin.indexJellyPath)))
                    .forEach(generated::add);
        }
        return generated;
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Scanned acc) {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree visit(@Nullable Tree tree, ExecutionContext ctx) {
                SourceFile sourceFile = (SourceFile) requireNonNull(tree);
                Path path = sourceFile.getSourcePath();
                String fileName = path.getFileName().toString();
                if ("index.jelly".equals(fileName)) {
                    acc.indexJellies.add(path.toString());
                } else if (Jenkins.isJenkinsPluginPom(sourceFile) != null) {
                    Xml.Document pom = (Xml.Document) sourceFile;
                    TagExtractor tags = new TagExtractor();
                    tags.visit(pom, ctx);
                    acc.plugins.add(new DescribedPlugin(
                            tags.artifactId,
                            path.resolve("../src/main/resources/index.jelly").normalize().toString(),
                            tags.description
                    ));
                }
                return sourceFile;
            }
        };
    }
    
    @Value
    private static class DescribedPlugin {
        String artifactId;
        String indexJellyPath;
        String pomDescription;

        String contents() {
            String desc = pomDescription.isEmpty() ? artifactId : pomDescription;
            return "<?jelly escape-by-default='true'?>\n" +
                    "<div>\n" +
                    desc + "\n" +
                    "</div>\n";
        }
    }

    static class Scanned {
        Set<String> indexJellies = new HashSet<>();
        Set<DescribedPlugin> plugins = new HashSet<>();
    }
    
    private static class TagExtractor extends MavenIsoVisitor<ExecutionContext> {
        private String artifactId = "";
        private String description = "";

        @Override
        public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag t = super.visitTag(tag, ctx);
            if ("description".equals(t.getName())) {
                description = t.getValue().orElse("");
            } else if ("artifactId".equals(t.getName()) && !isManagedDependencyTag() && !isDependencyTag()) {
                artifactId = t.getValue().orElseThrow(() -> new IllegalStateException("Expected to find an artifact id"));
            }
            return t;
        }
    }
}
