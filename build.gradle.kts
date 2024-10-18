import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.6"
  kotlin("plugin.spring") version "2.0.20"
  kotlin("plugin.jpa") version "2.0.20"
  id("org.jetbrains.kotlinx.kover") version "0.8.3"
  idea
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.7")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("org.apache.commons:commons-text:1.12.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")
  implementation("commons-codec:commons-codec:1.16.1")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  testRuntimeOnly("com.h2database:h2:2.3.230")

  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:1.0.7")
  testImplementation("org.wiremock:wiremock-standalone:3.9.1")
  testImplementation("io.swagger.parser.v3:swagger-parser:2.1.22") {
    exclude(group = "io.swagger.core.v3")
  }
}

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.tngtech.archunit:archunit-junit5-api:1.3.0")
  testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
  testImplementation("io.jsonwebtoken:jjwt-api:0.12.5")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.5")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.12.5")
}

kotlin {
jvmToolchain(21)
}

tasks {
withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
}
}
