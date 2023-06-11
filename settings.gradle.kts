rootProject.name = "rewrite-jenkins"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("junit-bom", "org.junit", "junit-bom").version {
                strictly("[5.9.1, 6.0.0[")
            }
            library("rewrite-bom", "org.openrewrite:rewrite-bom:8.1.0")
            library("rewrite-recipe-bom", "org.openrewrite.recipe:rewrite-recipe-bom:2.0.0")
        }
        create("kt") {
            val kotlin = version("kotlin", "1.8.21")
            plugin("jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlin)
        }
        create("nn") {
            plugin("contacts", "com.netflix.nebula.contacts").version("7.0.1")
            plugin("info", "com.netflix.nebula.info").version("12.1.4")
            plugin("javadoc-jar", "com.netflix.nebula.javadoc-jar").version("20.3.0")
            plugin("maven-manifest", "com.netflix.nebula.maven-manifest").version("20.3.0")
            plugin("maven-publish", "com.netflix.nebula.maven-publish").version("20.3.0")
            plugin("maven-resolved-dependencies", "com.netflix.nebula.maven-resolved-dependencies").version("20.3.0")
            plugin("publish-verification", "com.netflix.nebula.publish-verification").version("20.3.0")
            plugin("release", "com.netflix.nebula.release").version("17.2.2")
            plugin("source-jar", "com.netflix.nebula.source-jar").version("20.3.0")
        }
    }
}
