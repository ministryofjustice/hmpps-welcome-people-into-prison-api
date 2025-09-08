plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "9.0.1"
  kotlin("plugin.spring") version "2.2.10"
  kotlin("plugin.jpa") version "2.2.10"
  id("org.jetbrains.kotlinx.kover") version "0.9.1"
  idea
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")

  implementation("org.apache.commons:commons-text:1.14.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.11")
  implementation("commons-codec:commons-codec:1.19.0")
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.5.0")
  implementation("uk.gov.justice.service.hmpps:hmpps-digital-prison-reporting-lib:9.4.4")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  testRuntimeOnly("com.h2database:h2:2.3.232")

  testImplementation("org.wiremock:wiremock-standalone:3.13.1")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.tngtech.archunit:archunit-junit5-api:1.4.1")
  testImplementation("com.tngtech.archunit:archunit-junit5:1.4.1")
  testImplementation("io.jsonwebtoken:jjwt-api:0.13.0")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.13.0")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.13.0")
}
kotlin {
  jvmToolchain(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
  }
}
