package net.sghill.jenkins.rewrite

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.openrewrite.java.Assertions.java
import org.openrewrite.java.Assertions.srcMainJava
import org.openrewrite.java.JavaParser
import org.openrewrite.test.RecipeSpec
import org.openrewrite.test.RewriteTest


class UpgradeHtmlUnit330Test : RewriteTest {

    @Language("java")
    private val webClient2 = """
        package com.gargoylesoftware.htmlunit;
        import com.gargoylesoftware.htmlunit.html.HtmlPage;
        public class WebClient {
            public HtmlPage getPage(String in) { return null; }
        }
        """.trimIndent()
    
    @Language("java")
    private val htmlPage2 = """
        package com.gargoylesoftware.htmlunit.html;
        public class HtmlPage {
            public HtmlForm getFormByName(String in) { return null; }
        }
        """.trimIndent()
    
    @Language("java")
    private val htmlForm2 = """
        package com.gargoylesoftware.htmlunit.html;
        public class HtmlForm {
            public HtmlInput getInputByName(String in) { return null; }
        }
        """.trimIndent()
    
    @Language("java")
    private val htmlInput2 = """
        package com.gargoylesoftware.htmlunit.html;
        public class HtmlInput {
            public String getValueAttribute() { return ""; }
            public String getValue() { return ""; }
            public void setAttribute(String attributeName, String attributeValue) {}
            public void setValueAttribute(String newValue) {}
            public void setValue(String newValue) {}
        }
        """.trimIndent()

    @Language("java")
    private val webClient3 = """
        package org.htmlunit;
        import org.htmlunit.html.HtmlPage;
        public class WebClient {
            public HtmlPage getPage(String in) { return null; }
        }
        """.trimIndent()

    @Language("java")
    private val htmlPage3 = """
        package org.htmlunit.html;
        public class HtmlPage {
            public HtmlForm getFormByName(String in) { return null; }
        }
        """.trimIndent()

    @Language("java")
    private val htmlForm3 = """
        package org.htmlunit.html;
        public class HtmlForm {
            public HtmlInput getInputByName(String in) { return null; }
        }
        """.trimIndent()

    @Language("java")
    private val htmlInput3 = """
        package org.htmlunit.html;
        public class HtmlInput {
            public String getValueAttribute() { return ""; }
            public String getValue() { return ""; }
            public void setAttribute(String attributeName, String attributeValue) {}
            public void setValueAttribute(String newValue) {}
            public void setValue(String newValue) {}
        }
        """.trimIndent()

    override fun defaults(spec: RecipeSpec) {
        spec.parser(JavaParser.fromJavaVersion().dependsOn(webClient2, htmlPage2, htmlForm2, htmlInput2, webClient3, htmlPage3, htmlForm3, htmlInput3))
        spec.recipeFromResource("/META-INF/rewrite/rewrite.yml", "net.sghill.jenkins.rewrite.UpgradeHtmlUnit_3_3_0")
    }

    @Test
    fun htmlUnit() = rewriteRun(
        srcMainJava({ spec -> spec.path("net/sghill/example/HtmlUnitUse.java") },
            java("""
                package net.sghill.example;
                
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
            """.trimIndent(),
                """
                package net.sghill.example;
                
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
                """.trimIndent()
            )
        )
    )
}
