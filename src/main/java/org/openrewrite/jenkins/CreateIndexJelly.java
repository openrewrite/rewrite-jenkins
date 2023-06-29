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

import net.sghill.jenkins.rewrite.Jenkins;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Preconditions;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.SourceFile;
import org.openrewrite.Tree;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.tree.MavenResolutionResult;
import org.openrewrite.text.CreateTextFile;
import org.openrewrite.xml.search.FindTags;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.Path;
import java.util.Objects;

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
        return "The Jenkins tooling has changed over the years to [enforce](https://github.com/jenkinsci/maven-hpi-plugin/pull/302) " +
                "that a `src/main/resources/index.jelly` exists with a description. " +
                "If you don't have this, the build fails.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Scanned acc) {
        return Preconditions.check(acc.isJenkinsPlugin, new CreateTextFile(
                acc.description,
                acc.indexJellyPath.toString(),
                true
        ).getVisitor());
    }

    @Override
    public Scanned getInitialValue(ExecutionContext ctx) {
        return new Scanned();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Scanned acc) {
        return Preconditions.check(!acc.isJenkinsPlugin, new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public @Nullable Tree visit(@Nullable Tree tree, ExecutionContext executionContext) {
                SourceFile sourceFile = (SourceFile) Objects.requireNonNull(tree);
                if (!acc.isJenkinsPlugin) {
                    if (Jenkins.isJenkinsPluginPom(sourceFile) != null) {
                        Xml.Document pom = (Xml.Document) sourceFile;
                        acc.description = FindTags.find(pom, "/project/description").stream()
                                .findAny()
                                .flatMap(Xml.Tag::getValue)
                                .orElse(pom.getMarkers().findFirst(MavenResolutionResult.class)
                                        .map(maven -> maven.getPom().getArtifactId())
                                        .orElseThrow(() -> new IllegalStateException("Expected to find an artifact id")));
                        acc.indexJellyPath = sourceFile.getSourcePath().resolve("../src/main/resources/index.jelly").normalize();
                        acc.isJenkinsPlugin = true;
                    }
                }
                return sourceFile;
            }
        });
    }

    static class Scanned {
        boolean isJenkinsPlugin;
        String description;
        Path indexJellyPath;
    }
}
