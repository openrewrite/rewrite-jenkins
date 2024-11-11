/*
 * Copyright 2024 the original author or authors.
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
import org.openrewrite.DocumentExample;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.test.SourceSpecs.text;

class AddJellyXmlDeclarationTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new AddJellyXmlDeclaration());
    }

    @DocumentExample
    @Test
    void addJellyXmlDeclaration() {
        rewriteRun(
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

    @Test
    void addXmlDeclarationToJellyFile() {
        rewriteRun(
          spec -> spec.expectedCyclesThatMakeChanges(1),
          //language=xml
          text(
            """
              <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
                  <st:contentType value="text/html"/>
                  <h1>Hello, World!</h1>
              </j:jelly>
              """,
            """
              <?jelly escape-by-default='true'?>
              <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
                  <st:contentType value="text/html"/>
                  <h1>Hello, World!</h1>
              </j:jelly>
              """,
            spec -> spec.path("example.jelly")
          )
        );
    }

    @Test
    void doNotAddXmlDeclarationIfAlreadyPresent() {
        //language=xml
        rewriteRun(
          spec -> spec.expectedCyclesThatMakeChanges(0),
          text(
            """
              <?jelly escape-by-default='true'?>
              <j:jelly xmlns:j="jelly:core" xmlns:st="jelly:stapler" xmlns:d="jelly:define">
                  <st:contentType value="text/html"/>
                  <h1>Hello, World!</h1>
              </j:jelly>
              """,
            spec -> spec.path("example.jelly")
          )
        );
    }
}
