// Define variables for project configurations
val projectGroup = "dev.onelenyk"
val appId = "crudfather"
val mainAppClassName = "$projectGroup.$appId.ApplicationKt"

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

val project_version: String by project

plugins {
    kotlin("jvm") version "1.9.10"
    id("io.ktor.plugin") version "2.3.3"
    kotlin("plugin.serialization") version "1.9.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("org.jetbrains.dokka") version "1.9.10"
    application // Apply the application plugin to add support for building a CLI application in Java.
}

group = projectGroup
version = project_version

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation(kotlin("stdlib"))

    // Ktor dependencies
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-server-html-builder:$ktor_version")
    implementation("io.ktor:ktor-server-host-common:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")
    implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")

    //Koin
    implementation( "io.insert-koin:koin-core:3.4.3")

    // Logging
    implementation("ch.qos.logback:logback-classic:$logback_version")
    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

}

application {
    // Define the main class for the application.
    mainClass.set(mainAppClassName)
}

tasks.shadowJar {
    archiveClassifier.set("")
    manifest {
        attributes["Main-Class"] = mainAppClassName
    }
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = mainAppClassName
        attributes["Implementation-Title"] = project.name
        attributes["Implementation-Version"] = project.version
    }
    archiveFileName.set("app.jar")
}


// dokka

tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

// Custom tasks to group dependencies
tasks.register("prepareForPublication") {
    dependsOn("dokkaJavadocJar", "shadowJar")
}

tasks.register("prepareDistribution") {
    dependsOn("prepareForPublication", "jar")
}

tasks.register("setupScripts") {
    dependsOn("prepareForPublication", "startShadowScripts")
}

// Ensure proper dependencies
val dependentTasks = listOf("distZip", "distTar", "startScripts", "shadowDistZip", "shadowDistTar", "startShadowScripts")

dependentTasks.forEach { taskName ->
    tasks.named(taskName) {
        dependsOn(tasks.named("prepareForPublication"))
    }
}

tasks.named("startShadowScripts") {
    mustRunAfter(tasks.named("jar"))
}

ktor {
    fatJar {
        archiveFileName.set("${project.name}-${version}-fat.jar")
    }
}
