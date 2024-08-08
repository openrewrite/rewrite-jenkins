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

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.*;
import org.openrewrite.groovy.GroovyIsoVisitor;
import org.openrewrite.groovy.GroovyParser;
import org.openrewrite.groovy.tree.G;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.Markup;

@Value
@EqualsAndHashCode(callSuper = false)
public class UpgradeJavaVersion extends Recipe {

    @Option(displayName = "Java version",
            description = "The Java version to upgrade to.",
            example = "17")
    Integer version;

    @Option(displayName = "Distribution",
            description = "The distribution of Java to use. When omitted the current distribution is maintained.",
            example = "openjdk")
    @Nullable
    String distribution;

    @Override
    public String getDisplayName() {
        return "Upgrade Jenkins Java version";
    }

    @Override
    public String getDescription() {
        return "Upgrades the version of java specified in Jenkins groovy scripts. " +
               "Will not downgrade if the version is newer than the specified version.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return Preconditions.check(new FindSourceFiles("**/Jenkinsfile"), new GroovyIsoVisitor<ExecutionContext>() {
            @Override
            public J.Assignment visitAssignment(J.Assignment assignment, ExecutionContext ctx) {
                J.Assignment a = super.visitAssignment(assignment, ctx);
                if (!(a.getVariable() instanceof J.Identifier) || !(a.getAssignment() instanceof J.Literal)) {
                    return a;
                }
                J.Identifier id = (J.Identifier) a.getVariable();
                J.Literal value = (J.Literal) a.getAssignment();
                if (!("java_version".equals(id.getSimpleName()) || "javaVersion".equals(id.getSimpleName())) || !(value.getValue() instanceof String)) {
                    return a;
                }
                String currentJdkString = ((String) value.getValue()).trim();
                int versionBeginsIndex = StringUtils.indexOf(currentJdkString, Character::isDigit);
                String currentJdkDistribution = currentJdkString.substring(0, versionBeginsIndex);
                String currentJdkVersion = currentJdkString.substring(versionBeginsIndex);
                int jdkVersion;
                try {
                    jdkVersion = Integer.parseInt(currentJdkVersion);
                } catch (NumberFormatException e) {
                    return Markup.warn(a, new IllegalStateException("Unable to parse JDK version", e));
                }
                String targetVersion = currentJdkVersion;
                if (jdkVersion < version) {
                    targetVersion = String.valueOf(version);
                }
                String targetDistribution = currentJdkDistribution;
                if (distribution != null) {
                    targetDistribution = distribution;
                }
                String targetJdkString = targetDistribution + targetVersion;
                getCursor().putMessageOnFirstEnclosing(J.Lambda.class, "TARGET_JDK_ALERADY_CONFIGURED", true);
                if (!targetJdkString.equals(currentJdkString)) {
                    char quote = value.getValueSource() == null ? '\'' : value.getValueSource().charAt(0);
                    a = a.withAssignment(value.withValue(targetJdkString)
                            .withValueSource(quote + targetJdkString + quote));
                }
                return a;
            }


            @Override
            public J.Lambda visitLambda(J.Lambda lambda, ExecutionContext ctx) {
                J.Lambda l = super.visitLambda(lambda, ctx);
                if (!(getCursor().getParentTreeCursor().getValue() instanceof J.MethodInvocation)) {
                    return l;
                }
                J.MethodInvocation m = getCursor().getParentTreeCursor().getValue();
                if (!m.getSimpleName().equals("scmCheckout")) {
                    return l;
                }

                if (!(l.getBody() instanceof J.Block) || getCursor().pollMessage("TARGET_JDK_ALERADY_CONFIGURED") != null) {
                    return l;
                }
                J.Assignment as = GroovyParser.builder().build()
                        .parse("java_version = '" + distribution + version + "'")
                        .findFirst()
                        .map(G.CompilationUnit.class::cast)
                        .map(cu -> cu.getStatements().get(0))
                        .map(J.Assignment.class::cast)
                        .orElse(null);
                if (as == null) {
                    return l;
                }
                J.Block body = (J.Block) l.getBody();
                as = autoFormat(as, ctx, new Cursor(getCursor(), body));
                l = l.withBody(body.withStatements(ListUtils.concat(body.getStatements(), as)));
                return l;
            }
        });
    }
}
