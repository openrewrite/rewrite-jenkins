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
package org.openrewrite.jenkins.github;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.intellij.lang.annotations.Language;
import org.openrewrite.*;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.yaml.ChangePropertyValue;
import org.openrewrite.yaml.JsonPathMatcher;
import org.openrewrite.yaml.YamlIsoVisitor;
import org.openrewrite.yaml.YamlParser;
import org.openrewrite.yaml.tree.Yaml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddSecurityScanWorkflow extends ScanningRecipe<AtomicBoolean> {
    private static final String FILE_PATH = ".github/workflows/jenkins-security-scan.yml";
    private static final String DEFAULT_WORKFLOW_PATH = "/org/openrewrite/jenkins/github/jenkins-security-scan.yml";
    private static final JsonPathMatcher BRANCHES_KEY = new JsonPathMatcher("$.on.push.branches");
    private static final String JAVA_VERSION_KEY = "jobs.security-scan.with.java-version";
    private static final String BUILD_TOOL_KEY = "jobs.security-scan.with.java-cache";

    @Option(displayName = "Branches",
            description = "Run workflow on push to these branches.",
            example = "main",
            required = false)
    @Nullable
    List<String> branches;

    @Option(displayName = "Java Version",
            description = "Version of Java to set for build.",
            example = "11",
            required = false)
    @Nullable
    Integer javaVersion;

    @Option(displayName = "Build Tool",
            description = "Set up dependency cache.",
            example = "maven",
            valid = {"maven", "gradle"},
            required = false)
    @Nullable
    String buildTool;

    @Override
    public String getDisplayName() {
        return "Add Jenkins Security Scan workflow";
    }

    @Override
    public String getDescription() {
        return "Adds the Jenkins Security Scan GitHub Actions workflow. " +
                "See [docs](https://www.jenkins.io/doc/developer/security/scan/) for details.";
    }

    @Override
    public AtomicBoolean getInitialValue(ExecutionContext ctx) {
        return new AtomicBoolean();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(AtomicBoolean found) {
        Path path = Paths.get(FILE_PATH);
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree visit(@Nullable Tree tree, ExecutionContext executionContext, Cursor parent) {
                SourceFile sourceFile = (SourceFile) requireNonNull(tree);
                if (path.toString().equals(sourceFile.getSourcePath().toString())) {
                    found.set(true);
                }
                return sourceFile;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(AtomicBoolean found, ExecutionContext ctx) {
        if (found.get()) {
            return Collections.emptyList();
        }
        YamlParser parser = new YamlParser();
        String workflow = defaultWorkflow();
        return parser.parse(workflow)
                .map(brandNewFile -> (Yaml.Documents) brandNewFile.withSourcePath(Paths.get(FILE_PATH)))
                .collect(Collectors.toList());
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(AtomicBoolean found) {
        return new YamlIsoVisitor<ExecutionContext>() {
            @Override
            public Yaml.Documents visitDocuments(Yaml.Documents documents, ExecutionContext executionContext) {
                if (branches != null) {
                    doAfterVisit(new ReplaceSequenceVisitor(BRANCHES_KEY, branches));
                }
                if (javaVersion != null) {
                    doAfterVisit(new ChangePropertyValue(
                            JAVA_VERSION_KEY,
                            String.valueOf(javaVersion),
                            null,
                            null,
                            false
                    ).getVisitor());
                }
                if (buildTool != null) {
                    doAfterVisit(new ChangePropertyValue(
                            BUILD_TOOL_KEY,
                            buildTool,
                            null,
                            null,
                            false
                    ).getVisitor());
                }
                return super.visitDocuments(documents, executionContext);
            }
        };
    }

    private static String defaultWorkflow() {
        try (InputStream is = AddSecurityScanWorkflow.class.getResourceAsStream(DEFAULT_WORKFLOW_PATH)) {
            requireNonNull(is);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, UTF_8))) {
                List<String> wanted = new LinkedList<>();
                List<String> lines = br.lines().collect(Collectors.toList());
                boolean licenseHeaderDone = false;
                for (String line : lines) {
                    if (licenseHeaderDone) {
                        wanted.add(line);
                    } else if (line.isEmpty()) {
                        licenseHeaderDone = true;
                    }
                }
                return String.join("\n", wanted);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
