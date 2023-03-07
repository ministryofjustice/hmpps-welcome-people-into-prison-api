package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase

@Suppress("ClassName")
class LogResourceTest : IntegrationTestBase() {

  @Test
  fun `generate info log`() {
    val token = getAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read"))
    webTestClient.get()
      .uri("/log-test/info/1")
      .withBearerToken(token)
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .exchange()
      .expectStatus().isOk
  }
}
