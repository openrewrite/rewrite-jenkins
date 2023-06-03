import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension

plugins {
    `java-library`

    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.nebula.contacts)
    alias(libs.plugins.nebula.info)
    alias(libs.plugins.nebula.release)

    id("nebula.maven-manifest") version "17.3.2"
    id("nebula.maven-nebula-publish") version "17.3.2"
    id("nebula.maven-resolved-dependencies") version "17.3.2"

    id("nebula.javadoc-jar") version "17.3.2"
    id("nebula.source-jar") version "17.3.2"
}

apply(plugin = "nebula.publish-verification")

configure<nebula.plugin.release.git.base.ReleasePluginExtension> {
    defaultVersionStrategy = nebula.plugin.release.NetflixOssStrategies.SNAPSHOT(project)
}

group = "net.sghill.jenkins"
description = "Jenkins Rewrite recipes."

repositories {
    mavenCentral()
}

//The bom version can also be set to a specific version or latest.release.
val rewriteBomVersion = "1.19.4"

dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    compileOnly("com.google.code.findbugs:jsr305:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")
    implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:${rewriteBomVersion}"))
    implementation(platform("org.openrewrite:rewrite-bom:7.40.8"))

    implementation("org.rocksdb:rocksdbjni:7.2.2")
    implementation("org.openrewrite:rewrite-java")
    implementation("org.openrewrite:rewrite-maven")
    implementation("org.openrewrite:rewrite-yaml")
    runtimeOnly("org.openrewrite:rewrite-java-11")

    implementation("org.apache.maven.indexer:indexer-core:6.+")
    implementation("com.google.inject:guice:latest.release")
    implementation("org.eclipse.sisu:org.eclipse.sisu.inject:latest.release")
    implementation("org.apache.maven.wagon:wagon-http-lightweight:latest.release")
//    implementation("org.eclipse.aether:aether-api:latest.release")
//    implementation("org.eclipse.aether:aether-impl:latest.release")
//    implementation("org.apache.maven:waggon-http-lightweight:latest.release")

    testImplementation("org.ow2.asm:asm:latest.release")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation(platform(libs.junit.bom))
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.openrewrite:rewrite-java-tck")
    testImplementation("org.assertj:assertj-core:latest.release")
    
    testRuntimeOnly("com.github.spotbugs:spotbugs-annotations:4.7.0")
    testRuntimeOnly("com.google.code.findbugs:jsr305:3.0.2")
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.36")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+UnlockDiagnosticVMOptions", "-XX:+ShowHiddenFrames")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configure<ContactsExtension> {
    val j = Contact("team@moderne.io")
    j.moniker("Team Moderne")
    people["team@moderne.io"] = j
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("-parameters"))
}

configure<PublishingExtension> {
    publications {
        named("nebula", MavenPublication::class.java) {
            suppressPomMetadataWarningsFor("runtimeElements")
        }
    }
}

publishing {
  repositories {
      maven {
          name = "moderne"
          url = uri("https://us-west1-maven.pkg.dev/moderne-dev/moderne-recipe")
      }
  }
}
