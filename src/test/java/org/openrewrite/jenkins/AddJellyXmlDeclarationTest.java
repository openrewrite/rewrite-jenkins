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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.openrewrite.test.SourceSpecs.text;

/**
 * Test class for the AddJellyXmlDeclaration recipe.
 */
class AddJellyXmlDeclarationTest implements RewriteTest {

    /**
     * Configures default test settings by:
     * - Setting up the AddJellyXmlDeclaration recipe
     * - Configuring the PlainTextParser for processing Jelly files
     *
     * @param spec the RecipeSpec object used to configure the test
     */
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddJellyXmlDeclaration());
    }

    /**
     * Test to verify that the XML declaration is added to a simple Jelly file.
     */
    @DocumentExample
    @Test
    void addJellyXmlDeclaration() {
        rewriteRun(
          spec -> spec.expectedCyclesThatMakeChanges(1),
          //language=xml
          text(
            """
              <j:jelly xmlns:j="jelly:core">
                <h1>Simple Example</h1>
              </j:jelly>
              """,
            """
              <?jelly escape-by-default='true'?>
              <j:jelly xmlns:j="jelly:core">
                <h1>Simple Example</h1>
              </j:jelly>
              """,
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
        //language=xml
        String input = """
          <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
              <st:contentType value="text/html"/>
              <h1>Hello, World!</h1>
          </j:jelly>
          """;
        //language=xml
        String expected = """
          <?jelly escape-by-default='true'?>
          <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
              <st:contentType value="text/html"/>
              <h1>Hello, World!</h1>
          </j:jelly>
          """;
        Path inputFile = tempDir.resolve("example.jelly");
        Path expectedFile = tempDir.resolve("expected.jelly");
        try {
            Files.writeString(inputFile, input);
            Files.writeString(expectedFile, expected);

            rewriteRun(
              spec -> spec.expectedCyclesThatMakeChanges(1),
              text(Files.readString(inputFile), Files.readString(expectedFile)));
        } finally {
            try {
                Files.deleteIfExists(inputFile);
                Files.deleteIfExists(expectedFile);
            } catch (IOException e) {
                // Log warning but don't fail the test
                System.err.println("Warning: Failed to clean up test files: " + e.getMessage());
            }
        }
    }

    /**
     * Test to verify that the XML declaration is not added if it is already present.
     *
     * @param tempDir a temporary directory provided by JUnit
     * @throws IOException if an I/O error occurs
     */
    @Test
    void doNotAddXmlDeclarationIfAlreadyPresent(@TempDir Path tempDir) throws IOException {
        //language=xml
        String input = """
          <?jelly escape-by-default='true'?>
          <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
              <st:contentType value="text/html"/>
              <h1>Hello, World!</h1>
          </j:jelly>
          """;
        Path inputFile = tempDir.resolve("example.jelly");
        try {
            Files.writeString(inputFile, input);
            rewriteRun(
              spec -> spec.expectedCyclesThatMakeChanges(0),
              text(Files.readString(inputFile)));
        } finally {
            try {
                Files.deleteIfExists(inputFile);
            } catch (IOException e) {
                // Log warning but don't fail the test
                System.err.println("Warning: Failed to clean up test files: " + e.getMessage());
            }
        }
    }
}
