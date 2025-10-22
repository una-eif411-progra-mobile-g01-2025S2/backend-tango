// Importaciones de plugins y tareas
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Plugins principales
plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	kotlin("kapt") version "1.9.25"
}

// Configuración de grupo y versión
// --------------------------------
group = "cr.una.pai"
version = "0.0.1-SNAPSHOT"

// Configuración de Java
// --------------------------------
java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

// Repositorios
// --------------------------------
repositories {
	mavenCentral()
}

// Dependencias
// --------------------------------
dependencies {
	// MapStruct para mapeo de objetos
	implementation("org.mapstruct:mapstruct:1.6.0")
	kapt("org.mapstruct:mapstruct-processor:1.6.0")

	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-webflux") // WebFlux para WebClient (OpenRouter)
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	// Seguridad y JWT
	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
	testImplementation("org.springframework.security:spring-security-test")

	// Jackson y Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Procesadores de configuración
	kapt("org.springframework.boot:spring-boot-configuration-processor")

	// Base de datos
	implementation("org.postgresql:postgresql:42.7.3")

	// Migraciones de base de datos (Flyway)
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")

	// Librería JSON para IA Service
	implementation("org.json:json:20231013")
}

// Configuración de Kapt
// --------------------------------
kapt {
	correctErrorTypes = true
}

// Opciones de compilación de Kotlin
// --------------------------------
tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "21"
	}
}

// Configuración de pruebas
// --------------------------------
tasks.withType<Test> {
	useJUnitPlatform()
}
