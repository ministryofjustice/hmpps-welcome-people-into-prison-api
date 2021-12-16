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
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferInDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.PrisonerAndPncNumber
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson
import java.time.LocalDateTime

@Suppress("ClassName")
class TransfersResourceTest : IntegrationTestBase() {

  @Nested
  inner class `Find transfers on day` {
    @BeforeEach
    fun beforeEach() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI")
      prisonerSearchMockServer.stubMatchByPrisonerNumbers(200, listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA", pncNumber = "1234/1234589A")))
    }

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prisons/MDI/transfers/enroute")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prisons/MDI/transfers/enroute")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI")

      webTestClient.get().uri("/prisons/MDI/transfers/enroute")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("transfers".loadJson(this))
    }

    @Test
    fun `calls prison API with passed through token`() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI")

      val token = getAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read"))

      webTestClient.get().uri("/prisons/MDI/transfers/enroute")
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk

      prisonApiMockServer.verify(
        getRequestedFor(
          urlEqualTo(
            "/api/movements/MDI/enroute"
          )
        ).withHeader("Authorization", equalTo(token))
      )
    }
  }

  @Nested
  inner class `Find transfer on day` {
    @BeforeEach
    fun beforeEach() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI")
      prisonerSearchMockServer.stubMatchByPrisonerNumbers(200, listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA", pncNumber = "1234/1234589A")))
    }

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prisons/MDI/transfers/enroute/G6081VQ")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prisons/MDI/transfers/enroute/G6081VQ")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI")

      webTestClient.get().uri("/prisons/MDI/transfers/enroute/G6081VQ")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("transfer".loadJson(this))
    }

    @Test
    fun `calls prison API with passed through token`() {
      prisonApiMockServer.stubGetPrisonTransfersEnRoute("MDI")

      val token = getAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read"))

      webTestClient.get().uri("/prisons/MDI/transfers/enroute/G6081VQ")
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk

      prisonApiMockServer.verify(
        getRequestedFor(
          urlEqualTo(
            "/api/movements/MDI/enroute"
          )
        ).withHeader("Authorization", equalTo(token))
      )
    }
  }

  companion object {
    val transferInDetail = TransferInDetail(
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

      val transferInDetails = TransferInDetail(
        "MDI-RECP",
        "some comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      val token = getAuthorisation(roles = listOf("ROLE_BOOKING_CREATE"), scopes = listOf("write"))

      webTestClient
        .post()
        .uri("/transfers/A1234BC/confirm")
        .bodyValue(transferInDetail)
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk

      prisonApiMockServer.verify(
        putRequestedFor(
          urlEqualTo(
            "/api/offenders/A1234BC/transfer-in"
          )
        ).withHeader("Authorization", equalTo(token))
          .withRequestBody(equalToJson(objectMapper.writeValueAsString(transferInDetails)))
      )
    }
  }
}
