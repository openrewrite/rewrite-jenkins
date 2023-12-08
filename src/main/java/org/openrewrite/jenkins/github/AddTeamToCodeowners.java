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

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextParser;
import org.openrewrite.text.PlainTextVisitor;
import org.openrewrite.xml.XPathMatcher;
import org.openrewrite.xml.tree.Xml;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class AddTeamToCodeowners extends ScanningRecipe<AddTeamToCodeowners.Scanned> {
    private static final String FILE_PATH = ".github/CODEOWNERS";

    @Override
    public String getDisplayName() {
        return "Add plugin developer team to CODEOWNERS";
    }

    @Override
    public String getDescription() {
        return "Adds the `{artifactId}-plugin-developers` team to all files in `.github/CODEOWNERS` if absent.";
    }

    @Override
    public Scanned getInitialValue(ExecutionContext ctx) {
        return new Scanned(new ArtifactIdTeamNameGenerator(), new InMemoryTeamNameValidator());
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Scanned acc) {
        return new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree visit(@Nullable Tree tree, ExecutionContext ctx, Cursor parent) {
                SourceFile sourceFile = (SourceFile) requireNonNull(tree);
                Path path = sourceFile.getSourcePath();
                String fileName = path.getFileName().toString();
                if ("CODEOWNERS".equals(fileName)) {
                    acc.foundFile = true;
                } else if (acc.artifactId == null && "pom.xml".equals(fileName)) {
                    Xml.Document pom = (Xml.Document) sourceFile;
                    ArtifactIdExtractor extractor = new ArtifactIdExtractor();
                    extractor.visit(pom, ctx);
                    acc.artifactId = extractor.artifactId;
                }
                return sourceFile;
            }
        };
    }

    @Override
    public Collection<? extends SourceFile> generate(Scanned acc, ExecutionContext ctx) {
        if (acc.foundFile || !acc.hasValidTeamName()) {
            return Collections.emptyList();
        }
        PlainTextParser parser = new PlainTextParser();
        String line = "* " + acc.teamName() + "\n";
        return parser.parse(line)
                .map(brandNewFile -> (PlainText) brandNewFile.withSourcePath(Paths.get(FILE_PATH)))
                .collect(Collectors.toList());
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Scanned acc) {
        return Preconditions.check(acc.hasValidTeamName(), new PlainTextVisitor<ExecutionContext>() {
            @Override
            public PlainText visitText(PlainText plainText, ExecutionContext ctx) {
                if (!FILE_PATH.equals(plainText.getSourcePath().toString())) {
                    return plainText;
                }
                String text = plainText.getText();
                if (acc.presentIn(text)) {
                    return plainText;
                }
                boolean endsWithNewLine = text.endsWith("\n");
                List<String> lines = new LinkedList<>();
                List<String> after = new LinkedList<>();
                try (Scanner scanner = new Scanner(text)) {
                    int atPos = 0;
                    boolean lastComment = true;
                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if (atPos == 0 && line.contains("@")) {
                            atPos = line.indexOf("@");
                        }
                        if (lastComment && line.startsWith("#")) {
                            lines.add(line);
                        } else {
                            lastComment = false;
                            after.add(line);
                        }
                    }
                    int spaces = Math.max(1, atPos - 1);
                    lines.add("*" + StringUtils.repeat(" ", spaces) + acc.teamName());
                    lines.addAll(after);
                    String updated = String.join("\n", lines);
                    if (endsWithNewLine) {
                        updated += "\n";
                    }
                    return plainText.withText(updated);
                }
            }
        });
    }

    @Data
    public static class Scanned {
        private final TeamNameGenerator<TeamNameInput> generator;
        private final TeamNameValidator validator;
        String artifactId;
        boolean foundFile;

        public Scanned(TeamNameGenerator<TeamNameInput> generator, TeamNameValidator validator) {
            this.generator = generator;
            this.validator = validator;
        }

        boolean presentIn(String text) {
            Pattern p = Pattern.compile("^\\*\\s+" + teamName() + "\\s*$");
            try (Scanner s = new Scanner(text)) {
                while (s.hasNextLine()) {
                    String line = s.nextLine();
                    Matcher matcher = p.matcher(line);
                    if (matcher.matches()) {
                        return true;
                    }
                }
                return false;
            }
        }

        String teamName() {
            return generator.generate(new TeamNameInput(artifactId));
        }

        boolean hasValidTeamName() {
            return validator.isValid(teamName());
        }
    }

    private static class ArtifactIdExtractor extends MavenIsoVisitor<ExecutionContext> {
        private String artifactId = "";
        private static final XPathMatcher PROJECT_ARTIFACTID_MATCHER = new XPathMatcher("/project/artifactId");

        @Override
        public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext ctx) {
            Xml.Tag t = super.visitTag(tag, ctx);
            if (PROJECT_ARTIFACTID_MATCHER.matches(getCursor())) {
                artifactId = t.getValue().orElseThrow(() -> new IllegalStateException("Expected to find an artifact id"));
            }
            return t;
        }
    }
}
