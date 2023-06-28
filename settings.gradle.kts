rootProject.name = "rewrite-jenkins"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("junit-bom", "org.junit", "junit-bom").version {
                strictly("[5.9.1, 6.0.0[")
            }
            library("rewrite-bom", "org.openrewrite:rewrite-bom:8.1.2")
            library("rewrite-recipe-bom", "org.openrewrite.recipe:rewrite-recipe-bom:2.0.1")
        }
        create("kt") {
            val kotlin = version("kotlin", "1.8.21")
            plugin("jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlin)
        }
    }
}
