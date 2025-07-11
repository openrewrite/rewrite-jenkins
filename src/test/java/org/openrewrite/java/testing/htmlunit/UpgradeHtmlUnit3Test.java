/*
 * Copyright 2024 the original author or authors.
 * <p>
 * Licensed under the Moderne Source Available License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://docs.moderne.io/licensing/moderne-source-available-license
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.java.testing.htmlunit;

import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openrewrite.java.Assertions.java;
import static org.openrewrite.maven.Assertions.pomXml;

class UpgradeHtmlUnit3Test implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec
          .parser(JavaParser.fromJavaVersion().classpath("htmlunit"))
          .recipeFromResource("/META-INF/rewrite/htmlunit.yml", "org.openrewrite.java.testing.htmlunit.UpgradeHtmlUnit_3");
    }

    @DocumentExample
    @Test
    void shouldUpdateHtmlUnit() {
        rewriteRun(
          //language=java
          java(
            """
              import com.gargoylesoftware.htmlunit.WebClient;
              import com.gargoylesoftware.htmlunit.html.HtmlForm;
              import com.gargoylesoftware.htmlunit.html.HtmlInput;
              import com.gargoylesoftware.htmlunit.html.HtmlPage;

              import java.io.IOException;

              public class HtmlUnitUse {
                  void run() throws IOException {
                      try (WebClient webClient = new WebClient()) {
                          HtmlPage page = webClient.getPage("https://htmlunit.sourceforge.io/");
                          HtmlForm form = page.getFormByName("config");
                          HtmlInput a = form.getInputByName("a");
                          String value = a.getValueAttribute();
                          assert "".equals(value);
                          a.setAttribute("value", "up2");
                          a.setAttribute("value2", "leave");
                          a.setValueAttribute("updated");
                      }
                  }
              }
              """,
            """
              import org.htmlunit.WebClient;
              import org.htmlunit.html.HtmlForm;
              import org.htmlunit.html.HtmlInput;
              import org.htmlunit.html.HtmlPage;

              import java.io.IOException;

              public class HtmlUnitUse {
                  void run() throws IOException {
                      try (WebClient webClient = new WebClient()) {
                          HtmlPage page = webClient.getPage("https://htmlunit.sourceforge.io/");
                          HtmlForm form = page.getFormByName("config");
                          HtmlInput a = form.getInputByName("a");
                          String value = a.getValue();
                          assert "".equals(value);
                          a.setAttribute("value", "up2");
                          a.setAttribute("value2", "leave");
                          a.setValue("updated");
                      }
                  }
              }
              """
          )
        );
    }

    @Test
    void shouldUpgradeHtmlUnitDriver() {
        rewriteRun(
          pomXml(
            //language=xml
            """
              <project>
                <groupId>something</groupId>
                <artifactId>example-driver</artifactId>
                <version>1.0-SNAPSHOT</version>
                <dependencyManagement>
                  <dependencies>
                    <dependency>
                      <groupId>org.seleniumhq.selenium</groupId>
                      <artifactId>htmlunit-driver</artifactId>
                      <version>3.64.0</version>
                    </dependency>
                  </dependencies>
                </dependencyManagement>
                <dependencies>
                  <dependency>
                    <groupId>org.seleniumhq.selenium</groupId>
                    <artifactId>htmlunit-driver</artifactId>
                  </dependency>
                </dependencies>
              </project>
              """,
            spec -> spec.after(actual ->
              assertThat(actual)
                .containsPattern("<artifactId>htmlunit3-driver</artifactId>")
                .containsPattern("<version>4.\\d+.\\d+</version>")
                .actual()
            )
          )
        );
    }
}
