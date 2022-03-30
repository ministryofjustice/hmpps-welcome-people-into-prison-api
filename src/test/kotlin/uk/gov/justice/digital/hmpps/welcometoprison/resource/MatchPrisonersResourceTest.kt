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
  fun `Can search with PNC number only`() {
    prisonerSearchMockServer.stubMatchPrisoners(HttpStatus.OK.value())
    val validRequest = """
        {
          "pncNumber": "1234/1234589A"
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

  @Test
  fun `Can search with Prison number only`() {
    prisonerSearchMockServer.stubMatchPrisoners(HttpStatus.OK.value())
    val validRequest = """
        {
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

    assertThat(resp!!).hasSize(1)
    with(resp[0]) {
      assertThat(lastName).isEqualTo("Smith")
      assertThat(firstName).isEqualTo("Jim")
      assertThat(prisonNumber).isEqualTo("A1278AA")
      assertThat(dateOfBirth).isEqualTo(LocalDate.of(1991, 7, 31))
    }
  }

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

    assertThat(resp!!).hasSize(1)
    with(resp[0]) {
      assertThat(lastName).isEqualTo("Larsen")
      assertThat(firstName).isEqualTo("Robert")
      assertThat(prisonNumber).isEqualTo("A1234AA")
      assertThat(dateOfBirth).isEqualTo(LocalDate.of(1975, 4, 2))
    }
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

    assertThat(resp!!).hasSize(2)

    with(resp[0]) {
      assertThat(lastName).isEqualTo("Larsen")
      assertThat(firstName).isEqualTo("Robert")
      assertThat(prisonNumber).isEqualTo("A1234AA")
      assertThat(dateOfBirth).isEqualTo(LocalDate.of(1975, 4, 2))
    }

    with(resp[1]) {
      assertThat(lastName).isEqualTo("Larsen")
      assertThat(firstName).isEqualTo("Robert")
      assertThat(prisonNumber).isEqualTo("A1233AA")
      assertThat(dateOfBirth).isEqualTo(LocalDate.of(1975, 4, 2))
    }
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

    assertThat(resp).isEmpty()
  }
}
