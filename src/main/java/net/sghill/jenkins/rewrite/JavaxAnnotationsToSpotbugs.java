package net.sghill.jenkins.rewrite;

import org.openrewrite.Recipe;
import org.openrewrite.java.ChangePackage;
import org.openrewrite.java.ChangeType;

public class JavaxAnnotationsToSpotbugs extends Recipe {
    @Override
    public String getDisplayName() {
        return "javax.annotations to SpotBugs";
    }
    
    public JavaxAnnotationsToSpotbugs() {
        doNext(new ChangeType("javax.annotation.Nonnull", "edu.umd.cs.findbugs.annotations.NonNull", true));
        doNext(new ChangePackage("javax.annotation", "edu.umd.cs.findbugs.annotations", false));
    }
}
