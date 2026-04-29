plugins {
    kotlin("jvm") version "2.3.20"
    kotlin("plugin.spring") version "2.3.20"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.spruhs"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val cucumberVersion = "7.22.0"

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Spring Boot Test (enthält JUnit 5)
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Cucumber
    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-spring:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")

    // JUnit Platform Suite (für den Cucumber-Runner)
    testImplementation("org.junit.platform:junit-platform-suite:1.11.4")

    // Datenbanken
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.postgresql:postgresql")
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // JWT
    testImplementation("io.jsonwebtoken:jjwt-api:0.12.6")
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Kotlin Test
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}