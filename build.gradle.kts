// Importaciones de plugins y tareas
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Plugins principales
plugins {
	id("org.springframework.boot") version "3.2.5"
	id("io.spring.dependency-management") version "1.1.5"
	kotlin("jvm") version "1.9.25"
	kotlin("kapt") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
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
	// MapStruct (Kotlin + kapt)
	implementation("org.mapstruct:mapstruct:1.6.0")
	kapt("org.mapstruct:mapstruct-processor:1.6.0")

	// Spring Boot
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.flywaydb:flyway-core:11.14.1")
	implementation("org.flywaydb:flyway-database-postgresql:11.14.1")
	runtimeOnly("org.postgresql:postgresql")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")

	// Test
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// Configuración de Kapt
// --------------------------------
kapt {
	correctErrorTypes = true
	arguments {
		arg("mapstruct.defaultComponentModel", "spring")
		arg("mapstruct.defaultInjectionStrategy", "constructor")
		arg("mapstruct.unmappedTargetPolicy", "WARN")
	}
}

// Opciones de compilación de Kotlin
// --------------------------------
tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "21"
	}
}

// Configuración de pruebas
// --------------------------------
tasks.withType<Test> {
	useJUnitPlatform()
}
