rootProject.name = "rewrite-jenkins"

enableFeaturePreview("VERSION_ORDERING_V2")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("junit-bom", "org.junit", "junit-bom").version {
                strictly("[5.9.1, 6.0.0[")
            }
        }
    }
}
