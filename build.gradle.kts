import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    kotlin("plugin.serialization") version "1.6.20"
    application
    jacoco
    id("io.gitlab.arturbosch.detekt") version "1.20.0-RC2"
    id("com.osacky.doctor") version "0.8.0"
}

group = "com.octawizard"
version = "0.1-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://dl.bintray.com/kotlin/ktor")
    }
    maven {
        url = uri("https://dl.bintray.com/kotlin/kotlinx")
    }
}

val ktorVersion = "1.6.8"
val exposedVersion = "0.37.3"
val kmongoVersion = "4.5.1"

dependencies {
    testImplementation(kotlin("test-junit5"))
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("org.litote.kmongo:kmongo-flapdoodle:$kmongoVersion") {
        exclude("org.jetbrains.kotlin", "kotlin-test-junit")
    }
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")

    testRuntimeOnly("com.h2database:h2:2.1.210")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.8.2")

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-serialization:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
    // Dependency Injection
    implementation("org.kodein.di:kodein-di-jvm:7.11.0")

    // orm
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)

    // driver and connection pool
    implementation("org.postgresql:postgresql:42.4.1")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // configuration
    implementation("com.typesafe:config:1.4.2")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.slf4j:slf4j-log4j12:1.7.36")

    // redis
    implementation("org.redisson:redisson:3.17.0")

    // kmongo
    implementation("org.litote.kmongo:kmongo-id:$kmongoVersion")
    implementation("org.litote.kmongo:kmongo-native:$kmongoVersion")

    // detekt
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.20.0-RC2")
}

jacoco {
    toolVersion = "0.8.7"
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    reports {
        xml.isEnabled = true
    }
    dependsOn(tasks.test)
}

application {
    mainClassName = "ServerKt"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config =
        files("$projectDir/config/detekt.yml") // point to your custom config defining rules to run, overwriting default behavior
    baseline = file("$projectDir/config/baseline.xml") // a way of suppressing issues before introducing detekt
}

tasks.detekt.configure { //.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true) // observe findings in your browser with structure and code snippets
        xml.required.set(true) // checkstyle like format mainly for integrations like Jenkins
        txt.required.set(true) // similar to the console output, contains issue signature to manually edit baseline files
        sarif.required.set(true) // standardized SARIF format (https://sarifweb.azurewebsites.net/) to support integrations with Github Code Scanning
    }
    jvmTarget = "11"
}

tasks.withType<io.gitlab.arturbosch.detekt.DetektCreateBaselineTask>().configureEach {
    jvmTarget = "11"
}
