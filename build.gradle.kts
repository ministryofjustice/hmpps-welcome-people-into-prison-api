plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.15.3"
  kotlin("plugin.spring") version "1.9.22"
  kotlin("plugin.jpa") version "1.9.22"
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

  implementation("org.apache.commons:commons-text:1.11.0")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
  implementation("commons-codec:commons-codec:1.16.1")

  runtimeOnly("org.flywaydb:flyway-core")
  runtimeOnly("org.postgresql:postgresql:42.7.2")
  testRuntimeOnly("com.h2database:h2:2.2.224")

  testImplementation("org.wiremock:wiremock-standalone:3.4.1")

  testImplementation("org.springframework.security:spring-security-test")
  testImplementation("com.tngtech.archunit:archunit-junit5-api:1.2.1")
  testImplementation("com.tngtech.archunit:archunit-junit5:1.2.1")
  testImplementation("io.jsonwebtoken:jjwt-api:0.12.5")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.12.5")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.12.5")
  constraints {
    implementation("org.json:json:20230618") {
      because("previous transitive version has CVE-2022-45688")
    }
  }
}
java {
  toolchain.languageVersion = JavaLanguageVersion.of(21)
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
      apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
      freeCompilerArgs.set(listOf("-Xemit-jvm-type-annotations"))
      jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
      languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1)
    }
  }
}
