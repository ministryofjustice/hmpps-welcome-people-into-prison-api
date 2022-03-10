package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.PotentialMatch
import java.time.LocalDate

@Suppress("ClassName")
class MatchPrisonersResourceTest : IntegrationTestBase() {

  @Test
  fun `search result with 1 matches`() {
    prisonerSearchMockServer.stubMatchPrisonerByNameAndDateOfBirthOneResult()
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
    assertThat(resp?.get(0)?.lastName).isEqualTo("Larsen")
    assertThat(resp?.get(0)?.firstName).isEqualTo("Robert")
    assertThat(resp?.get(0)?.dateOfBirth).isEqualTo(LocalDate.of(1975, 4, 2))
  }
  @Test
  fun `search result with 2 matches`() {
    prisonerSearchMockServer.stubMatchPrisonerByNameAndDateOfBirthTwoResult()
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

    assertThat(resp?.size).isEqualTo(2)
    assertThat(resp?.get(0)?.lastName).isEqualTo("Larsen")
    assertThat(resp?.get(0)?.firstName).isEqualTo("Robert")
    assertThat(resp?.get(0)?.prisonNumber).isEqualTo("A1234AA")
    assertThat(resp?.get(0)?.dateOfBirth).isEqualTo(LocalDate.of(1975, 4, 2))
    assertThat(resp?.get(1)?.lastName).isEqualTo("Larsen")
    assertThat(resp?.get(1)?.firstName).isEqualTo("Robert")
    assertThat(resp?.get(1)?.prisonNumber).isEqualTo("A1233AA")
    assertThat(resp?.get(1)?.dateOfBirth).isEqualTo(LocalDate.of(1975, 4, 2))
  }

  @Test
  fun `search result with 0 matches`() {
    prisonerSearchMockServer.stubMatchPrisonerByNameAndDateOfBirthZeroResult()
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
