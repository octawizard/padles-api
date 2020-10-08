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
dependencies {
    testImplementation(kotlin("test-junit5"))
    testImplementation("org.testcontainers:testcontainers:1.14.3")
    testImplementation("org.testcontainers:junit-jupiter:1.14.3")
    testImplementation("io.mockk:mockk:1.10.2")
    testRuntimeOnly("com.h2database:h2:1.4.200")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.0.0")

    implementation("io.ktor:ktor-server-netty:1.4.0")
    implementation("io.ktor:ktor-html-builder:1.4.0")
    implementation("io.ktor:ktor-gson:1.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.2")

    // Dependency Injection
    implementation("org.kodein.di:kodein-di-jvm:7.1.0")

    //dao
    implementation("org.jetbrains.exposed", "exposed-core", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-dao", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-jdbc", "0.24.1")
    implementation("org.jetbrains.exposed", "exposed-java-time", "0.24.1")

    // driver and connection pool
    implementation("org.postgresql:postgresql:42.2.16")
    implementation("com.zaxxer:HikariCP:3.4.5")

    // configuration
    implementation("com.typesafe:config:1.4.0")

    // logging
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.3")
    implementation("org.apache.logging.log4j:log4j-core:2.13.3")
    implementation("org.slf4j:slf4j-log4j12:1.7.30")

    // redis
    implementation("org.redisson:redisson:3.13.5")

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
