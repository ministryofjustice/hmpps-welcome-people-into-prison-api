package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase

@Suppress("ClassName")
class UnexpectedArrivalsResourceTest : IntegrationTestBase() {
  @Nested
  @DisplayName("Confirm arrival")
  inner class ConfirmArrivalTests {
    private val validRequest = """
        {
          "firstName": "Alpha",
          "lastName": "Omega",
          "dateOfBirth": "1961-05-29",
          "sex": "M",
          "prisonId": "NMI",
          "movementReasonCode": "N",
          "imprisonmentStatus": "SENT03"
        }
      """

    @Test
    fun `create and book happy path`() {
      val prisonNumber = "AA1111A"
      val location = "Reception"
      prisonApiMockServer.stubCreateOffender(prisonNumber)

      webTestClient
        .post()
        .uri("/unexpected-arrivals/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(validRequest)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("prisonNumber").isEqualTo(prisonNumber)
        .jsonPath("location").isEqualTo(location)
    }

    @Test
    fun `create and book passes through auth token`() {
      val prisonNumber = "AA1111A"

      prisonApiMockServer.stubCreateOffender(prisonNumber)

      val token = getAuthorisation(
        roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
        scopes = listOf("read", "write"),
      )

      webTestClient
        .post()
        .uri("/unexpected-arrivals/confirm")
        .withBearerToken(token)
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(validRequest)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("prisonNumber").isEqualTo(prisonNumber)

      prisonApiMockServer.verify(
        postRequestedFor(
          urlEqualTo(
            "/api/offenders",
          ),
        ).withHeader("Authorization", equalTo(token)),
      )
    }

    @Test
    fun `create and book request validation failure`() {
      val prisonNumber = "AA1111A"

      prisonApiMockServer.stubCreateOffender(prisonNumber)

      webTestClient
        .post()
        .uri("/unexpected-arrivals/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          """
        {
          "dateOfBirth": "1961-05-29",
          "gender": "M",
          "prisonId": "NMI",
          "movementReasonCode": "N",
          "imprisonmentStatus": "SENT03"
        }
          """.trimIndent(),
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().json(
          """
        {
          "status": 400,
          "errorCode": null,
          "moreInfo": null
        }
          """.trimIndent(),
        )
    }

    @Test
    fun `create and book - prison-api create offender fails`() {
      prisonApiMockServer.stubCreateOffenderFails(500)

      webTestClient
        .post()
        .uri("/unexpected-arrivals/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(validRequest)
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
    }

    @Test
    fun `create and book - prison-api create offender fails on a client error without proper error response`() {
      prisonApiMockServer.stubCreateOffenderFails(400)

      webTestClient
        .post()
        .uri("/unexpected-arrivals/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(validRequest)
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
    }

    @Test
    fun `create and book - prison-api create booking fails because prisoner already exist`() {
      prisonApiMockServer.stubCreateOffenderFailsPrisonerAlreadyExist()

      webTestClient
        .post()
        .uri("/unexpected-arrivals/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(validRequest)
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody().json(
          """
        {
          "status": 400,
          "errorCode": "PRISONER_ALREADY_EXIST",
          "moreInfo": null
        }
          """.trimIndent(),
        )
    }
  }
}
