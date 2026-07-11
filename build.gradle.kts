plugins {
    java
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Bookstore"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

sourceSets {
    create("scripts") {
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testCompileOnly("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testAnnotationProcessor("org.projectlombok:lombok")
    "scriptsImplementation"("org.springframework.boot:spring-boot-starter-jdbc")
    "scriptsImplementation"("com.opencsv:opencsv:5.12.0")
    "scriptsImplementation"("net.datafaker:datafaker:2.7.0")
    "scriptsRuntimeOnly"("org.postgresql:postgresql")
}

tasks.register<JavaExec>("runScript") {
    group = "application"
    description = "Run a class from the scripts source set"

    classpath = sourceSets["scripts"].runtimeClasspath
    mainClass.set(project.findProperty("scriptClass") as String? ?: "com.example.TestingDbConnection")
    standardInput = System.`in`

    if (project.hasProperty("scriptArgs")) {
        args((project.property("scriptArgs") as String).split(" "))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
