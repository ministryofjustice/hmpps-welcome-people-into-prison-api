import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.1.1-beta-3"
  kotlin("plugin.spring") version "1.8.0"
  kotlin("plugin.jpa") version "1.8.0"
}

configurations {
  all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
  }
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

  implementation("org.apache.commons:commons-text:1.10.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.0.2")
  implementation("commons-codec:commons-codec:1.15")
  implementation("ch.qos.logback:logback-core:1.4.5")


  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.5.1")
  testRuntimeOnly("com.h2database:h2:2.1.214")

  testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:2.35.0")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.tngtech.archunit:archunit-junit5-api:1.0.1")
  testImplementation("com.tngtech.archunit:archunit-junit5:1.0.1")
  testImplementation("io.jsonwebtoken:jjwt-api:0.11.5")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.11.5")
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
      jvmTarget = "18"
    }
  }

  compileTestKotlin {
    kotlinOptions {
      jvmTarget = "18"
    }
  }
}
