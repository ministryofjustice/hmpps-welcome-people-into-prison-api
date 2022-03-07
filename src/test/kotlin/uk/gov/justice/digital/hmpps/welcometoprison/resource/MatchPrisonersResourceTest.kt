package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.PotentialMatch

@Suppress("ClassName")
class MatchPrisonersResourceTest : IntegrationTestBase() {

  @Test
  fun `search result with 1 matches`() {

    val validRequest = """
        {
          "firstName": "Robert",
          "lastName": "Larsen",
          "dateOfBirth": "1975-04-02"
        }
      """

    val token = getAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read"))

    val resp = webTestClient
      .post()
      .uri("/match-prisoners")
      .withBearerToken(token)
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(validRequest)
      .exchange()
      .expectStatus().isOk
      .expectBodyList(PotentialMatch::class.java).returnResult().responseBody

    assertThat(resp?.size).isEqualTo(1)
  }
  @Test
  fun `search result with 0 matches`() {

    val validRequest = """
        {
          "firstName": "Alpha",
          "lastName": "Omega",
          "dateOfBirth": "1961-05-29"
        }
      """

    val token = getAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read"))

    val resp = webTestClient
      .post()
      .uri("/match-prisoners")
      .withBearerToken(token)
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(validRequest)
      .exchange()
      .expectStatus().isOk
      .expectBodyList(PotentialMatch::class.java).returnResult().responseBody

    assertThat(resp.size).isEqualTo(0)
  }
}
