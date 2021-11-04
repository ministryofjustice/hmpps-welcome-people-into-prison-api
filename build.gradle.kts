import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "3.3.12"
  kotlin("plugin.spring") version "1.5.31"
  kotlin("plugin.jpa") version "1.5.31"
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
  implementation("com.github.javafaker:javafaker:1.0.2")

  implementation("org.springdoc:springdoc-openapi-webmvc-core:1.5.12")
  implementation("org.springdoc:springdoc-openapi-ui:1.5.12")
  implementation("org.springdoc:springdoc-openapi-kotlin:1.5.12")

  runtimeOnly("com.h2database:h2:1.4.200")
  runtimeOnly("org.flywaydb:flyway-core:8.0.3")
  runtimeOnly("org.postgresql:postgresql:42.3.1")

  testImplementation("io.jsonwebtoken:jjwt:0.9.1")
  testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("io.mockk:mockk:1.12.0")
}

allOpen {
  annotations(
    "javax.persistence.Entity",
    "javax.persistence.MappedSuperclass",
    "javax.persistence.Embeddable"
  )
}

/**
 * Without this Kotlin compiler setting Java Bean validator annotations do not work on Kotlin lists.
 */
tasks.withType<KotlinCompile>().configureEach {
  kotlinOptions {
    freeCompilerArgs += "-Xemit-jvm-type-annotations"
  }
}

tasks {
  compileKotlin {
    kotlinOptions {
      jvmTarget = "16"
    }
  }

  compileTestKotlin {
    kotlinOptions {
      jvmTarget = "16"
    }
  }
}
