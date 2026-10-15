package uk.gov.justice.digital.hmpps.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class DatasourceConfigurationTest {
  private val contextRunner = ApplicationContextRunner()
    .withInitializer(ConfigDataApplicationContextInitializer())

  @Test
  fun `dev datasource matches the Docker Compose database without external credentials`() {
    contextRunner.withPropertyValues("spring.profiles.active=dev").run { context ->
      assertThat(context).hasNotFailed()
      val environment = context.environment
      assertThat(environment.getProperty("spring.datasource.url"))
        .isEqualTo("jdbc:postgresql://localhost:5431/hmpps-welcome-people-into-prison-api-db")
      assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("test")
      assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("test")
      assertThat(environment.getProperty("spring.datasource.hikari.schema")).isEqualTo("public")
      assertThat(environment.getProperty("spring.flyway.url"))
        .isEqualTo(environment.getProperty("spring.datasource.url"))
      assertThat(environment.getProperty("spring.flyway.user")).isEqualTo("test")
      assertThat(environment.getProperty("spring.flyway.password")).isEqualTo("test")
    }
  }

  @Test
  fun `deployed datasource and Flyway use externally supplied settings`() {
    contextRunner.withPropertyValues(
      "database.endpoint=database.example:5432",
      "database.name=welcome",
      "spring.datasource.username=deployment-user",
      "spring.datasource.password=deployment-password",
    ).run { context ->
      assertThat(context).hasNotFailed()
      val environment = context.environment
      assertThat(environment.getProperty("spring.datasource.url"))
        .isEqualTo("jdbc:postgresql://database.example:5432/welcome?sslmode=prefer&currentSchema=welcome")
      assertThat(environment.getProperty("spring.datasource.username")).isEqualTo("deployment-user")
      assertThat(environment.getProperty("spring.datasource.password")).isEqualTo("deployment-password")
      assertThat(environment.getProperty("spring.datasource.hikari.schema")).isEqualTo("welcome")
      assertThat(environment.getProperty("spring.flyway.user")).isEqualTo("deployment-user")
      assertThat(environment.getProperty("spring.flyway.password")).isEqualTo("deployment-password")
    }
  }
}
