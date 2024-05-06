plugins {
    id("org.openrewrite.build.recipe-library") version "latest.release"
}

group = "org.openrewrite.recipe"
description = "Modernize Jenkins plugins. Automatically."

val rewriteVersion = rewriteRecipe.rewriteVersion.get()
dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    compileOnly("com.google.code.findbugs:jsr305:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")

    implementation(platform("org.openrewrite:rewrite-bom:$rewriteVersion"))

    implementation("org.openrewrite:rewrite-java")
    implementation("org.openrewrite:rewrite-maven")
    implementation("org.openrewrite:rewrite-yaml")
    implementation("org.openrewrite.recipe:rewrite-java-dependencies")
    runtimeOnly("org.openrewrite:rewrite-java-11")

    testImplementation("org.ow2.asm:asm:latest.release")

    testImplementation(platform("org.junit:junit-bom:latest.release"))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation(platform("org.mockito:mockito-bom:latest.release"))
    testImplementation("org.mockito:mockito-junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.openrewrite:rewrite-java-tck")
    testImplementation("org.assertj:assertj-core:latest.release")

    testRuntimeOnly("com.github.spotbugs:spotbugs-annotations:4.7.0")
    testRuntimeOnly("com.google.code.findbugs:jsr305:3.0.2")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.36")
}
