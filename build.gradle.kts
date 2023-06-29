import nebula.plugin.contacts.Contact
import nebula.plugin.contacts.ContactsExtension
import nebula.plugin.release.NetflixOssStrategies
import nebula.plugin.release.git.base.ReleasePluginExtension
import java.net.URI

plugins {
    id("org.openrewrite.build.recipe-library") version "latest.release"
    alias(kt.plugins.jvm)
}

configure<ReleasePluginExtension> {
    defaultVersionStrategy = NetflixOssStrategies.SNAPSHOT(project)
}

group = "net.sghill.jenkins"
description = "Jenkins Rewrite recipes."

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:latest.release")
    compileOnly("com.google.code.findbugs:jsr305:latest.release")
    annotationProcessor("org.projectlombok:lombok:latest.release")
    implementation(platform(libs.rewrite.recipe.bom))
    implementation(platform(libs.rewrite.bom))

    implementation("org.rocksdb:rocksdbjni:7.2.2")
    implementation("org.openrewrite:rewrite-java")
    implementation("org.openrewrite:rewrite-maven")
    implementation("org.openrewrite:rewrite-yaml")
    runtimeOnly("org.openrewrite:rewrite-java-11")

    implementation("org.apache.maven.indexer:indexer-core:6.+")
    implementation("com.google.inject:guice:6.+")
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

tasks.test {
    useJUnitPlatform()
    jvmArgs = listOf("-XX:+UnlockDiagnosticVMOptions", "-XX:+ShowHiddenFrames")
}

configure<ContactsExtension> {
    val j = Contact("sghill.dev@gmail.com")
    j.moniker("Steve Hill")
    j.github("sghill")
    people["sghill.dev@gmail.com"] = j
}

tasks.withType<JavaCompile>().configureEach {
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

val targetRepo: Provider<URI> = providers.provider {
    if (version.toString().endsWith("SNAPSHOT")) {
        uri("https://oss.sonatype.org/content/repositories/snapshots/")
    } else {
        uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
    }
}

publishing {
    repositories {
        repositories {
            maven {
                url = targetRepo.get()
                credentials {
                    username = providers.gradleProperty("ossrhUsername").getOrElse("Unknown user")
                    password = providers.gradleProperty("ossrhPassword").getOrElse("Unknown password")
                }
            }
        }
    }
    publications {
        named<MavenPublication>("nebula") {
            pom {
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/sghill/rewrite-jenkins.git")
                    developerConnection.set("scm:git:git@github.com:sghill/rewrite-jenkins.git")
                    url.set("https://github.com/sghill/rewrite-jenkins")
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["nebula"])
    useGpgCmd()
}

tasks.withType<Sign>() {
    onlyIf { gradle.taskGraph.hasTask(":publish") }
}
