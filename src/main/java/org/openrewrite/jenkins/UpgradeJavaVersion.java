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
    int version;

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
        return Preconditions.check(new FindSourceFiles("**/Jenkinsfile"),  new GroovyIsoVisitor<ExecutionContext>() {
            @Override
            public J.Assignment visitAssignment(J.Assignment assignment, ExecutionContext executionContext) {
                J.Assignment a = super.visitAssignment(assignment, executionContext);
                if(!(a.getVariable() instanceof J.Identifier) || !(a.getAssignment() instanceof J.Literal)) {
                    return a;
                }
                J.Identifier id = (J.Identifier) a.getVariable();
                J.Literal value = (J.Literal) a.getAssignment();
                if( !("java_version".equals(id.getSimpleName()) || "javaVersion".equals(id.getSimpleName())) || !(value.getValue() instanceof String)) {
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
                if(jdkVersion < version) {
                    targetVersion = String.valueOf(version);
                }
                String targetDistribution = currentJdkDistribution;
                if(distribution != null) {
                    targetDistribution = distribution;
                }
                String targetJdkString = targetDistribution + targetVersion;
                if(!targetJdkString.equals(currentJdkString)) {
                    char quote = value.getValueSource() == null ? '\'' : value.getValueSource().charAt(0);
                    a = a.withAssignment(value.withValue(targetJdkString)
                            .withValueSource(quote + targetJdkString + quote));
                }
                return a;
            }
        });
    }
}
