import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
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

val ktorVersion = "1.4.2"
val exposedVersion = "0.28.1"
val kmongoVersion = "4.2.3"

dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.testcontainers:testcontainers:1.15.0")
    testImplementation("org.testcontainers:junit-jupiter:1.15.0")
    testImplementation("io.mockk:mockk:1.10.3-jdk8")
    testImplementation("org.litote.kmongo:kmongo-flapdoodle:$kmongoVersion")

    testRuntimeOnly("com.h2database:h2:1.4.200")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.0.0")

    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-locations:$ktorVersion")

    // Dependency Injection
    implementation("org.kodein.di:kodein-di-jvm:7.1.0")

    // orm
    implementation("org.jetbrains.exposed", "exposed-core", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-dao", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-jdbc", exposedVersion)
    implementation("org.jetbrains.exposed", "exposed-java-time", exposedVersion)

    // driver and connection pool
    implementation("org.postgresql:postgresql:42.2.18")
    implementation("com.zaxxer:HikariCP:3.4.5")

    // configuration
    implementation("com.typesafe:config:1.4.1")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.3")
    implementation("org.apache.logging.log4j:log4j-core:2.14.0")
    implementation("org.slf4j:slf4j-log4j12:1.7.30")

    // redis
    implementation("org.redisson:redisson:3.14.0")

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
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}
tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
application {
    mainClassName = "ServerKt"
}
