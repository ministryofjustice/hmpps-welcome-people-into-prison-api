package uk.gov.justice.digital.hmpps.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class DevConfigurationTest {
  @Test
  fun `dev Prison API endpoint matches the Docker Compose port`() {
    ApplicationContextRunner()
      .withInitializer(ConfigDataApplicationContextInitializer())
      .withPropertyValues("spring.profiles.active=dev")
      .run { context ->
        assertThat(context).hasNotFailed()
        assertThat(context.environment.resolveRequiredPlaceholders("\${prison.endpoint.url}"))
          .isEqualTo("http://localhost:8082")
      }
  }
}
