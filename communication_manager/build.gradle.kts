import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootBuildImage
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("idea")
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.spring") version "1.9.23"
    kotlin("plugin.jpa") version "1.9.23"
}

group = "it.polito.wa2.g13"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.apache.camel.springboot:camel-spring-boot-starter:4.5.0")
    implementation("org.apache.camel.springboot:camel-google-mail-starter:4.5.0")
    implementation("org.apache.camel.springboot:camel-jackson-starter:4.5.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.camel:camel-google-mail:4.5.0")
    implementation("org.apache.camel:camel-http:4.5.0")
    implementation("org.apache.camel:camel-jms:4.5.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.apache.camel:camel-test-spring-junit5:4.5.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "21"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<BootJar> {
    exclude("secrets.yaml")
}

tasks.named<BootBuildImage>("bootBuildImage") {
    imageName = "${System.getenv("DOCKER_USERNAME")}/${project.name}:${project.version}"
    docker {
        publishRegistry {
            username = System.getenv("DOCKER_USERNAME")
            password = System.getenv("DOCKER_PASSWORD")
        }
    }
}

abstract class ProjectVersion : DefaultTask() {
    @TaskAction
    fun action() {
        print(project.version)
    }
}

tasks.register<ProjectVersion>("projectVersion")
