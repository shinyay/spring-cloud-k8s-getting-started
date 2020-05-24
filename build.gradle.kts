import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.3.0.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("com.google.cloud.tools.jib") version "2.1.0"
	kotlin("jvm") version "1.3.72"
	kotlin("plugin.spring") version "1.3.72"
	kotlin("plugin.jpa") version "1.3.72"
	kotlin("plugin.noarg") version "1.3.72"
}

group = "com.google.shinyay"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "Hoxton.SR4"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework.cloud:spring-cloud-starter-kubernetes-all")
	implementation("org.springframework.cloud:spring-cloud-starter")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

val username= if (hasProperty("docker_username")) findProperty("docker_username") as String else ""
val password = if (hasProperty("docker_password")) findProperty("docker_password") as String else ""

jib {
	from {
//		image = "shinyay/adoptopenjdk11-minimum"
		image = "openjdk:11-slim"
	}
	to {
		image = "registry.hub.docker.com/shinyay/${project.name}:${project.version}"
		auth.username = username
		auth.password = password
	}
	container {
		jvmFlags = mutableListOf("-Xms512m", "-Xdebug")
		creationTime = "USE_CURRENT_TIMESTAMP"
	}
}