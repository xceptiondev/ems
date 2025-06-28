plugins {
	kotlin("jvm") version "2.0.0"
	kotlin("plugin.spring") version "2.0.0"
	id("org.springframework.boot") version "3.5.1"
	id("io.spring.dependency-management") version "1.1.7"
	id("com.vaadin") version "24.8.0"
	kotlin("plugin.jpa") version "2.0.0"
}

group = "com.ems"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["vaadinVersion"] = "24.8.0"

dependencies {
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.0")
	implementation("com.vaadin:vaadin-spring-boot-starter:24.8.0")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

dependencyManagement {
	imports {
		mavenBom("com.vaadin:vaadin-bom:${property("vaadinVersion")}")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
