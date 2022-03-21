package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest

@Suppress("ClassName")
class CourtReturnsResourceTest : IntegrationTestBase() {

  private val confirmCourtReturnRequest = ConfirmCourtReturnRequest("NMI", "A1278AA")
  private val moveId = "06274b73-6aa9-490e-ab0e-2a25b3638068"
  private val url = "/court-returns/$moveId/confirm"

  @Test
  fun `confirm court return`() {
    val prisonNumber = "A1278AA"
    prisonerSearchMockServer.stubGetActivePrisoner(200)
    prisonApiMockServer.stubCourtTransferInOffender(prisonNumber)
    val location = "Reception"
    webTestClient
      .post()
      .uri(url)
      .headers(
        setAuthorisation(
          roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
          scopes = listOf("read", "write")
        )
      )
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(confirmCourtReturnRequest)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("prisonNumber").isEqualTo(prisonNumber)
      .jsonPath("location").isEqualTo(location)
  }

  @Test
  fun `requires authentication`() {
    webTestClient.post().uri(url)
      .bodyValue(confirmCourtReturnRequest)
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `requires correct role`() {
    val token = getAuthorisation(roles = listOf(), scopes = listOf("write"))

    webTestClient.post().uri(url)
      .bodyValue(confirmCourtReturnRequest)
      .withBearerToken(token)
      .exchange()
      .expectStatus().isForbidden
      .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
  }
}
