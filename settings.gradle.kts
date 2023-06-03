rootProject.name = "rewrite-jenkins"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val kotlin = version("kotlin", "1.7.21")
            library("junit-bom", "org.junit", "junit-bom").version {
                strictly("[5.9.1, 6.0.0[")
            }
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlin)
            plugin("nebula-contacts", "com.netflix.nebula.contacts").version("7.0.1")
            plugin("nebula-info", "com.netflix.nebula.info").version("12.1.4")
            plugin("nebula-javadoc", "com.netflix.nebula.javadoc-jar").version("20.3.0")
            plugin("nebula-release", "com.netflix.nebula.release").version("17.2.2")
        }
    }
}
