import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    application
    jacoco
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

val ktorVersion = "1.5.2"
val exposedVersion = "0.37.3"
val kmongoVersion = "4.2.4"

dependencies {
    testImplementation(kotlin("test-junit5"))
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("org.litote.kmongo:kmongo-flapdoodle:$kmongoVersion")
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
    implementation("org.postgresql:postgresql:42.3.3")
    implementation("com.zaxxer:HikariCP:5.0.1")

    // configuration
    implementation("com.typesafe:config:1.4.2")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.slf4j:slf4j-log4j12:1.7.30")

    // redis
    implementation("org.redisson:redisson:3.17.0")

    // kmongo
    implementation("org.litote.kmongo:kmongo-id:$kmongoVersion")
    implementation("org.litote.kmongo:kmongo-native:$kmongoVersion")
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
//
application {
    mainClassName = "ServerKt"
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
