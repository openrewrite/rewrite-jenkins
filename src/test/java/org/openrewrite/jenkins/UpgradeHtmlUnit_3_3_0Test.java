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

import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.openrewrite.DocumentExample;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class UpgradeHtmlUnit_3_3_0Test implements RewriteTest {
    @Language("java")
    private final String webClient2 = """
      package com.gargoylesoftware.htmlunit;
      import com.gargoylesoftware.htmlunit.html.HtmlPage;
      public class WebClient {
          public HtmlPage getPage(String in) { return null; }
      }
      """;

    @Language("java")
    private final String htmlPage2 = """
      package com.gargoylesoftware.htmlunit.html;
      public class HtmlPage {
          public HtmlForm getFormByName(String in) { return null; }
      }
      """;

    @Language("java")
    private final String htmlForm2 = """
      package com.gargoylesoftware.htmlunit.html;
      public class HtmlForm {
          public HtmlInput getInputByName(String in) { return null; }
      }
      """;

    @Language("java")
    private final String htmlInput2 = """
      package com.gargoylesoftware.htmlunit.html;
      public class HtmlInput {
          public String getValueAttribute() { return ""; }
          public String getValue() { return ""; }
          public void setAttribute(String attributeName, String attributeValue) {}
          public void setValueAttribute(String newValue) {}
          public void setValue(String newValue) {}
      }
      """;

    @Language("java")
    private final String webClient3 = """
      package org.htmlunit;
      import org.htmlunit.html.HtmlPage;
      public class WebClient {
          public HtmlPage getPage(String in) { return null; }
      }
      """;

    @Language("java")
    private final String htmlPage3 = """
      package org.htmlunit.html;
      public class HtmlPage {
          public HtmlForm getFormByName(String in) { return null; }
      }
      """;

    @Language("java")
    private final String htmlForm3 = """
      package org.htmlunit.html;
      public class HtmlForm {
          public HtmlInput getInputByName(String in) { return null; }
      }
      """;

    @Language("java")
    private final String htmlInput3 = """
      package org.htmlunit.html;
      public class HtmlInput {
          public String getValueAttribute() { return ""; }
          public String getValue() { return ""; }
          public void setAttribute(String attributeName, String attributeValue) {}
          public void setValueAttribute(String newValue) {}
          public void setValue(String newValue) {}
      }
      """;

    @Override
    public void defaults(RecipeSpec spec) {
        spec.parser(JavaParser.fromJavaVersion().dependsOn(webClient2, htmlPage2, htmlForm2, htmlInput2, webClient3, htmlPage3, htmlForm3, htmlInput3));
        spec.recipeFromResource("/META-INF/rewrite/htmlunit-3.yml", "org.openrewrite.jenkins.UpgradeHtmlUnit_3_3_0");
    }

    @Test
    @DocumentExample
    void shouldUpdateHtmlUnit() {
        rewriteRun(
          java(
            """
              package org.example;

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
              package org.example;

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
}
