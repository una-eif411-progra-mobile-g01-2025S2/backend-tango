import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	kotlin("kapt") version "1.9.25"
}

group = "backend-tango"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// MapStruct core
	implementation("org.mapstruct:mapstruct:1.6.0")
	kapt("org.mapstruct:mapstruct-processor:1.6.0")
	kapt("org.springframework.boot:spring-boot-configuration-processor")

	// Spring Boot Starters
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-webflux") // Para WebClient

	// Flyway para migraciones
	implementation("org.flywaydb:flyway-core:10.17.0")
	implementation("org.flywaydb:flyway-database-postgresql:10.17.0")

	// Driver Postgres actualizado
	runtimeOnly("org.postgresql:postgresql:42.7.4")


	// PostgreSQL Driver
	implementation("org.postgresql:postgresql:42.7.3")


	// JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

	// Jackson para Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// JSON library para IA Service
	implementation("org.json:json:20231013")

	// Devtools (solo en desarrollo)
	developmentOnly("org.springframework.boot:spring-boot-devtools")

	// Tests
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
}

// Configuración Kapt
kapt {
	correctErrorTypes = true
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}