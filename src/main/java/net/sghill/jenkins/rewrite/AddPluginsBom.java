package net.sghill.jenkins.rewrite;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.maven.AddManagedDependency;
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
public class AddPluginsBom extends Recipe {
    @Option(displayName = "artifactId",
            description = "Middle part of `io.jenkins.tools.bom:bom-2.303.x:VERSION`",
            example = "bom-2.303.x")
    String bomName;
    
    @Option(displayName = "version",
            description = "Last part of `io.jenkins.tools.bom:bom-2.303.x:VERSION`",
            example = "1409.v7659b_c072f18")
    String bomVersion;

    @JsonCreator
    public AddPluginsBom(@JsonProperty("bomName") String bomName,
                         @JsonProperty("bomVersion") String bomVersion) {
        this.bomName = bomName;
        this.bomVersion = bomVersion;
    }

    @Override
    public String getDisplayName() {
        return "Add Jenkins Plugins bom";
    }

    @Override
    public String getDescription() {
        return "Adds official Jenkins plugins bom if any dependencies are present in it";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new MavenVisitor<ExecutionContext>() {
            private boolean bomAlreadyAdded = false;
            
            @Override
            public Xml visitTag(Xml.Tag tag, ExecutionContext executionContext) {
                if (!isManagedDependencyTag()) {
                    ResolvedDependency dependency = findDependency(tag);
                    if (dependency != null && inBom(dependency)) {
                        if (!bomAlreadyAdded) {
                            doNext(new AddManagedDependency(
                                    "io.jenkins.tools.bom",
                                    bomName,
                                    bomVersion,
                                    "import",
                                    "pom",
                                    null,
                                    null,
                                    true,
                                    null,
                                    false
                            ));
                            bomAlreadyAdded = true;
                        }
                        // taken from 
                        Xml.Tag version = tag.getChild("version").orElse(null);
                        return tag.withContent(ListUtils.map(tag.getContent(), c -> c == version ? null : c));
                    }
                }
                return super.visitTag(tag, executionContext);
            }

            /**
             * This needs to be replaced with something that can
             * look up what is in the added bom.
             *
             * @param dependency declared
             * @return true if dependency is in bom
             */
            private boolean inBom(ResolvedDependency dependency) {
                String groupId = dependency.getGroupId();
                return "org.jenkins-ci.plugins".equals(groupId);
            }
        };
    }
}
