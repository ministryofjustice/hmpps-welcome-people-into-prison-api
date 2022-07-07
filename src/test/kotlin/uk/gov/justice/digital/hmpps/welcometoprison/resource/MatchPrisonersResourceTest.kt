package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.PotentialMatch
import java.time.LocalDate

@Suppress("ClassName")
class MatchPrisonersResourceTest : IntegrationTestBase() {
  @Test
  fun `Safe to search with nothing`() {
    val validRequest = """
        {
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

    assertThat(resp!!).isEmpty()
  }

  @Test
  fun `Can search with request`() {
    prisonerSearchMockServer.stubMatchPrisoners(HttpStatus.OK.value())
    val validRequest = """
        {
          "firstName": "Robert",
          "lastName": "Larsen",
          "dateOfBirth": "1975-04-02",
          "pncNumber": "1234/1234589A",
          "prisonNumber": "A1278AA"
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

    assertThat(resp!!.size).isEqualTo(1)
    with(resp[0]) {
      assertThat(lastName).isEqualTo("Smith")
      assertThat(firstName).isEqualTo("Jim")
      assertThat(prisonNumber).isEqualTo("A1278AA")
      assertThat(dateOfBirth).isEqualTo(LocalDate.of(1991, 7, 31))
    }
  }
}
