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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.text.PlainText;
import org.openrewrite.text.PlainTextVisitor;

/**
 * Recipe to add an XML declaration to Jelly files.
 */
public class AddJellyXmlDeclaration extends Recipe {

    /**
     * Returns the display name of the recipe.
     *
     * @return the display name of the recipe
     */
    @Override
    public String getDisplayName() {
        return "Add XML declaration to Jelly files";
    }

    /**
     * Returns the description of the recipe.
     *
     * @return the description of the recipe
     */
    @Override
    public String getDescription() {
        return "Ensure the XML declaration `<?jelly escape-by-default='true'?>` is present in all `.jelly` files.";
    }

    /**
     * Returns a visitor that adds the XML declaration to Jelly files.
     *
     * @return a PlainTextVisitor that adds the XML declaration
     */
    @Override
    public PlainTextVisitor<ExecutionContext> getVisitor() {
        return new PlainTextVisitor<ExecutionContext>() {
            public static final String JELLY_DECLARATION = "<?jelly escape-by-default='true'?>";

            /**
             * Visits the text and adds the XML declaration if necessary.
             *
             * @param text the PlainText object representing the file content
             * @param ctx the execution context
            public PlainText visitText(PlainText text, ExecutionContext ctx) {
             */
            @Override
            public PlainText visitText(PlainText text, ExecutionContext executionContext) {
                if (text == null || text.getSourcePath() == null) {
                    return text;
                }
                if ("file.txt".equals(text.getSourcePath().toString()) || text.getSourcePath().toString().endsWith(".jelly")) {
                    String content = text.getText();
                    if (content.trim().isEmpty()) {
                        return text.withText(JELLY_DECLARATION);
                    }
                    String lineEnding = content.contains("\r\n") ? "\r\n" : "\n";
                    if (content.trim().toLowerCase().matches("^<\\?jelly\\s+[^>]*>") && !content.startsWith(JELLY_DECLARATION)) {
                        content = content.substring(content.indexOf(lineEnding) + lineEnding.length());
                    }
                    if (!content.startsWith(JELLY_DECLARATION)) {
                        content = JELLY_DECLARATION + lineEnding + content;
                        return text.withText(content);
                    }
                }
                return text;
            }
        };
    }
}
