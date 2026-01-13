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
package org.openrewrite.jenkins;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jspecify.annotations.Nullable;
import org.openrewrite.*;
import org.openrewrite.groovy.GroovyIsoVisitor;
import org.openrewrite.groovy.GroovyParser;
import org.openrewrite.groovy.tree.G;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.StringUtils;
import org.openrewrite.java.marker.JavaProject;
import org.openrewrite.java.tree.J;
import org.openrewrite.marker.Markup;

import java.util.concurrent.atomic.AtomicBoolean;

@EqualsAndHashCode(callSuper = false)
@Value
public class UpgradeJavaVersion extends ScanningRecipe<AtomicBoolean> {

    @Option(displayName = "Java version",
            description = "The Java version to upgrade to.",
            example = "17")
    Integer version;

    @Option(displayName = "Distribution",
            description = "The distribution of Java to use. When omitted the current distribution is maintained.",
            example = "openjdk")
    @Nullable
    String distribution;

    String displayName = "Upgrade jenkins java version";

    String description = "Upgrades the version of java specified in Jenkins groovy scripts. " +
               "Will not downgrade if the version is newer than the specified version.";

    @Override
    public AtomicBoolean getInitialValue(ExecutionContext ctx) {
        return new AtomicBoolean();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(AtomicBoolean foundJavaProject) {
        return Preconditions.check(!foundJavaProject.get(), new TreeVisitor<Tree, ExecutionContext>() {
            @Override
            public Tree visit(@SuppressWarnings("NullableProblems") Tree tree, ExecutionContext ctx) {
                if (tree.getMarkers().findFirst(JavaProject.class).isPresent()) {
                    foundJavaProject.set(true);
                }
                return tree;
            }
        });
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(AtomicBoolean foundJavaProject) {
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
                if (!"scmCheckout".equals(m.getSimpleName())) {
                    return l;
                }

                if (!(l.getBody() instanceof J.Block) || getCursor().pollMessage("TARGET_JDK_ALERADY_CONFIGURED") != null || !foundJavaProject.get()) {
                    return l;
                }
                J.Assignment as = GroovyParser.builder().build()
                        .parse("java_version = '" + (distribution == null ? "" : distribution) + version + "'")
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
                return l.withBody(body.withStatements(ListUtils.concat(body.getStatements(), as)));
            }
        });
    }
}
