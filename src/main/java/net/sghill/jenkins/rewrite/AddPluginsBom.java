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
package net.sghill.jenkins.rewrite;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Preconditions;
import org.openrewrite.ScanningRecipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.jenkins.BomLookup;
import org.openrewrite.marker.SearchResult;
import org.openrewrite.maven.AddManagedDependency;
import org.openrewrite.maven.AddManagedDependencyVisitor;
import org.openrewrite.maven.MavenIsoVisitor;
import org.openrewrite.maven.MavenVisitor;
import org.openrewrite.maven.RemoveRedundantDependencyVersions;
import org.openrewrite.maven.tree.ResolvedDependency;
import org.openrewrite.xml.tree.Xml;

/**
 * Inspired by {@link RemoveRedundantDependencyVersions}
 * and {@link AddManagedDependency} with two important differences:
 * 
 * 1. we only add the bom if there is a direct dependency present
 * 2. remove the version even if it does not match
 *
 * Jenkins is going to run with what is deployed anyway, so the declared
 * version here is really only impacting tests.
 */
@Value
@EqualsAndHashCode(callSuper = true)
public class AddPluginsBom extends ScanningRecipe<AddPluginsBom.Scanned> {
    @Option(displayName = "artifactId",
            description = "Middle part of `io.jenkins.tools.bom:bom-2.303.x:VERSION`.",
            example = "bom-2.303.x")
    String bomName;
    
    @Option(displayName = "version",
            description = "Last part of `io.jenkins.tools.bom:bom-2.303.x:VERSION`.",
            example = "1409.v7659b_c072f18")
    String bomVersion;

    @Override
    public String getDisplayName() {
        return "Add Jenkins Plugins BOM";
    }

    @Override
    public String getDescription() {
        return "Adds official Jenkins plugins bom if any dependencies are present in .";
    }

    @Override
    public Scanned getInitialValue(ExecutionContext ctx) {
        return new Scanned();
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getScanner(Scanned acc) {
        BomLookup lookup = new BomLookup();
        return Preconditions.check(acc.hasDependencyInBom, new MavenIsoVisitor<ExecutionContext>() {
            @Override
            public Xml.Document visitDocument(Xml.Document document, ExecutionContext executionContext) {
                if (acc.hasDependencyInBom) {
                    return SearchResult.found(document);
                }
                return super.visitDocument(document, executionContext);
            }

            @Override
            public Xml.Tag visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                Xml.Tag t = super.visitTag(tag, executionContext);
                if (!isManagedDependencyTag()) {
                    ResolvedDependency dependency = findDependency(tag);
                    if (dependency != null) {
                        acc.hasDependencyInBom = acc.hasDependencyInBom || lookup.inBom(dependency.getGroupId(), dependency.getArtifactId());
                    }
                }
                return t;
            }
        });
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor(Scanned acc) {
        return Preconditions.check(acc.hasDependencyInBom, new MavenVisitor<ExecutionContext>() {
            @Override
            public Xml visitDocument(Xml.Document document, ExecutionContext executionContext) {
                Xml maven = super.visitDocument(document, executionContext);
                doAfterVisit(new AddManagedDependencyVisitor(
                        "io.jenkins.tools.bom",
                        bomName,
                        bomVersion,
                        "import",
                        "pom",
                        null
                ));
                return maven;
            }
        });
    }
    
    static class Scanned {
        boolean hasDependencyInBom;
    }
}
