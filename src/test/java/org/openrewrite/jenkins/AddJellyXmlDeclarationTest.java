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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.text.PlainTextParser;

import static org.openrewrite.test.SourceSpecs.text;

/**
 * Test class for the AddJellyXmlDeclaration recipe.
 */
public class AddJellyXmlDeclarationTest implements RewriteTest {

    /**
     * Sets the default recipe for the test.
     *
     * @param spec the RecipeSpec object used to configure the test
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddJellyXmlDeclaration());
        spec.parser(PlainTextParser.builder());
    }

    /**
     * Test to verify that the XML declaration is added to a simple Jelly file.
     */
    @Test
    void addJellyXmlDeclaration() {
        rewriteRun(
          spec -> spec.expectedCyclesThatMakeChanges(1),
          text(
            "<root></root>",
            "<?jelly escape-by-default='true'?>\n<root></root>",
            spec -> spec.path("example.jelly")
          )
        );
    }

    /**
     * Test to verify that the XML declaration is added to a Jelly file.
     *
     * @param tempDir a temporary directory provided by JUnit
     * @throws IOException if an I/O error occurs
     */
    @Test
    void addXmlDeclarationToJellyFile(@TempDir Path tempDir) throws IOException {
        Path inputFile = tempDir.resolve("example.jelly");
        Files.writeString(inputFile, """
              <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
                  <st:contentType value="text/html"/>
                  <h1>Hello, World!</h1>
              </j:jelly>
          """);

        Path expectedFile = tempDir.resolve("expected.jelly");
        Files.writeString(expectedFile, """
              <?jelly escape-by-default='true'?>
              <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
                  <st:contentType value="text/html"/>
                  <h1>Hello, World!</h1>
              </j:jelly>
          """);

        rewriteRun(
          spec -> spec.recipe(new AddJellyXmlDeclaration())
            .expectedCyclesThatMakeChanges(1)
          text(Files.readString(inputFile), Files.readString(expectedFile)));
    }

    /**
     * Test to verify that the XML declaration is not added if it is already present.
     *
     * @param tempDir a temporary directory provided by JUnit
     * @throws IOException if an I/O error occurs
     */
    @Test
    void doNotAddXmlDeclarationIfAlreadyPresent(@TempDir Path tempDir) throws IOException {
        Path inputFile = tempDir.resolve("example.jelly");
        Files.writeString(inputFile, """
              <?jelly escape-by-default='true'?>
              <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
                  <st:contentType value="text/html"/>
                  <h1>Hello, World!</h1>
              </j:jelly>
          """);

        rewriteRun(
          spec -> spec.recipe(new AddJellyXmlDeclaration())
            .expectedCyclesThatMakeChanges(0)
          text(Files.readString(inputFile)));
    }
}
