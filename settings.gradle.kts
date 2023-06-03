rootProject.name = "rewrite-jenkins"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlin = version("kotlin", "1.7.21")
            library("junit-bom", "org.junit", "junit-bom").version {
                strictly("[5.9.1, 6.0.0[")
            }
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlin)
        }
    }
}
