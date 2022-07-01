package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferIn
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers.TransferInDetail
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Suppress("ClassName")
class TransfersResourceTest : IntegrationTestBase() {

  @Nested
  inner class `Find transfers on day` {
    @BeforeEach
    fun beforeEach() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI", LocalDate.of(2022, 6, 29))
      prisonerSearchMockServer.stubMatchByPrisonerNumbers(
        200,
        listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA", pncNumber = "1234/1234589A"))
      )
    }

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prisons/MDI/transfers")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get()
        .uri("/prisons/MDI/transfers")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI", LocalDate.now())

      webTestClient.get().uri("/prisons/MDI/transfers")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("transfers".loadJson(this))
    }

    @Test
    fun `calls prison API with passed through token`() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI", LocalDate.now())

      val token = getAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read"))

      webTestClient.get().uri("/prisons/MDI/transfers")
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk

      prisonApiMockServer.verify(
        getRequestedFor(
          urlEqualTo(
            "/api/movements/MDI/enroute?movementDate=" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
          )
        ).withHeader("Authorization", equalTo(token))
      )
    }
  }

  @Nested
  inner class `Find transfer on day` {
    @BeforeEach
    fun beforeEach() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI", LocalDate.of(2022, 6, 29))
      prisonerSearchMockServer.stubMatchByPrisonerNumbers(
        200,
        listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA", pncNumber = "1234/1234589A"))
      )
    }

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prisons/MDI/transfers/G6081VQ")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prisons/MDI/transfers/G6081VQ")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI", LocalDate.now())

      webTestClient.get().uri("/prisons/MDI/transfers/G6081VQ")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("transfer".loadJson(this))
    }

    @Test
    fun `calls prison API with passed through token`() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI", LocalDate.now())

      val token = getAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read"))

      webTestClient.get().uri("/prisons/MDI/transfers/G6081VQ")
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk

      prisonApiMockServer.verify(
        getRequestedFor(
          urlEqualTo(
            "/api/movements/MDI/enroute?movementDate=" + LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
          )
        ).withHeader("Authorization", equalTo(token))
      )
    }
  }

  companion object {
    val transferInDetail = TransferInDetail(
      "MDI",
      "MDI-RECP",
      "some comment",
      LocalDateTime.of(2021, 11, 15, 1, 0, 0)
    )
  }

  @Nested
  inner class `Transfer in` {
    @Test
    fun `Requires authentication`() {
      webTestClient
        .post()
        .uri("/transfers/A1234BC/confirm")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Requires correct role`() {

      webTestClient
        .post()
        .uri("/transfers/A1234BC/confirm")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("write")))
        .bodyValue(transferInDetail)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `Should call prison service with correct args`() {
      prisonApiMockServer.stubTransferInOffender("A1234BC")

      val transferIn = TransferIn(
        "MDI-RECP",
        "some comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      val token = getAuthorisation(roles = listOf("ROLE_TRANSFER_PRISONER"), scopes = listOf("write"))

      webTestClient
        .post()
        .uri("/transfers/A1234BC/confirm")
        .bodyValue(transferInDetail)
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("prisonNumber").isEqualTo("A1234BC")
        .jsonPath("location").isEqualTo("Reception")

      prisonApiMockServer.verify(
        putRequestedFor(
          urlEqualTo(
            "/api/offenders/A1234BC/transfer-in"
          )
        ).withHeader("Authorization", equalTo(token))
          .withRequestBody(equalToJson(objectMapper.writeValueAsString(transferIn)))
      )
    }
  }
}
