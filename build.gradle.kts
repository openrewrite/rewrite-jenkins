plugins {
    id("org.openrewrite.build.recipe-library") version "latest.release"
    id("org.openrewrite.build.moderne-source-available-license") version "latest.release"
}

group = "org.openrewrite.recipe"
description = "Modernize Jenkins plugins. Automatically."

val rewriteVersion = rewriteRecipe.rewriteVersion.get()
dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    compileOnly("com.google.code.findbugs:jsr305:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")

    implementation(platform("org.openrewrite:rewrite-bom:$rewriteVersion"))

    implementation("org.openrewrite:rewrite-groovy")
    implementation("org.openrewrite:rewrite-java")
    implementation("org.openrewrite:rewrite-maven")
    implementation("org.openrewrite:rewrite-yaml")
    implementation("org.openrewrite.recipe:rewrite-java-dependencies:$rewriteVersion")
    runtimeOnly("org.openrewrite:rewrite-java-21")

    testImplementation(platform("org.junit:junit-bom:5.14.2"))
    testImplementation(platform("org.mockito:mockito-bom:latest.release"))
    testImplementation("org.mockito:mockito-junit-jupiter")

    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.assertj:assertj-core:latest.release")
}

recipeDependencies {
    testParserClasspath("com.github.spotbugs:spotbugs-annotations:4.7.0")
    testParserClasspath("com.google.code.findbugs:jsr305:3.0.2")
    testParserClasspath("net.sourceforge.htmlunit:htmlunit:2.70.0")
}
