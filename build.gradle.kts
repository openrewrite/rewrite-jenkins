import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension

plugins {
    `java-library`

    id("org.jetbrains.kotlin.jvm") version "1.5.21"
    id("nebula.release") version "15.3.1"

    id("nebula.maven-manifest") version "17.3.2"
    id("nebula.maven-nebula-publish") version "17.3.2"
    id("nebula.maven-resolved-dependencies") version "17.3.2"

    id("nebula.contacts") version "5.1.0"
    id("nebula.info") version "9.3.0"

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
    mavenLocal()
    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    mavenCentral()
}

configurations.all {
    resolutionStrategy {
        cacheChangingModulesFor(0, TimeUnit.SECONDS)
        cacheDynamicVersionsFor(0, TimeUnit.SECONDS)
    }
}

//The bom version can also be set to a specific version or latest.release.
val rewriteBomVersion = "1.3.0"

dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    compileOnly("com.google.code.findbugs:jsr305:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")
    implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:${rewriteBomVersion}"))

    implementation("org.openrewrite:rewrite-java")
    implementation("org.openrewrite:rewrite-maven")
    runtimeOnly("org.openrewrite:rewrite-java-11")

    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    testImplementation("org.junit.jupiter:junit-jupiter-api:latest.release")
    testImplementation("org.junit.jupiter:junit-jupiter-params:latest.release")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:latest.release")

    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.assertj:assertj-core:latest.release")
    
    testRuntimeOnly("com.github.spotbugs:spotbugs-annotations:4.7.0")
    testRuntimeOnly("com.google.code.findbugs:jsr305:3.0.2")
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
