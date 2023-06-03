package net.sghill.jenkins.rewrite;

import org.openrewrite.Recipe;
import org.openrewrite.java.ChangePackage;
import org.openrewrite.java.ChangeType;
import org.openrewrite.java.OrderImports;

/**
 * Updates javax.annotations to Jenkins preferred replacement.
 */
public class JavaxAnnotationsToSpotbugs extends Recipe {
    @Override
    public String getDisplayName() {
        return "`javax.annotations` to SpotBugs";
    }

    @Override
    public String getDescription() {
        return "Jenkins is no longer using JSR-305, likely due to Jigsaw concerns about split packages.";
    }

    /**
     * Changes javax.annotation.Nonnull to spotbugs NonNull
     * Updates packages javax.annotation to spotbugs
     */
    public JavaxAnnotationsToSpotbugs() {
        doNext(new ChangeType("javax.annotation.Nonnull", "edu.umd.cs.findbugs.annotations.NonNull", true));
        doNext(new ChangePackage("javax.annotation", "edu.umd.cs.findbugs.annotations", false));
        doNext(new OrderImports(false));
    }
}
