package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson

@Suppress("ClassName")
class ArrivalsResourceTest : IntegrationTestBase() {

  @Nested
  inner class `Find movements on day` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prisons/MDI/arrivals?date=2020-01-02")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prisons/MDI/arrivals?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `requires date param`() {
      webTestClient.get().uri("/prisons/MDI/arrivals")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Missing request value")
    }

    @Test
    fun `requires date param in correct format`() {
      webTestClient.get().uri("/prisons/MDI/arrivals?date=wibble")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Argument type mismatch")
    }

    @Test
    fun `returns json in expected format`() {
      prisonerSearchMockServer.stubMatchPrisoners(200)
      basmApiMockServer.stubGetPrison(200)
      basmApiMockServer.stubGetMovements(200)

      webTestClient.get().uri("/prisons/MDI/arrivals?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("arrivals".loadJson(this))
    }

    @Test
    fun `calls service method with correct args`() {
      prisonerSearchMockServer.stubMatchPrisoners(200)
      basmApiMockServer.stubGetPrison(200)

      webTestClient.get().uri("/prisons/MDI/arrivals?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      basmApiMockServer.verify(
        getRequestedFor(
          urlEqualTo(
            "/api/moves?include=profile.person,from_location,to_location,profile.person.gender&filter%5Bto_location_id%5D=a2bc2abf-75fe-4b7f-bf5a-a755bc290757&filter%5Bdate_from%5D=2020-01-02&filter%5Bdate_to%5D=2020-01-02&filter%5Bstatus%5D=requested,accepted,booked,in_transit,completed&page=1&per_page=200&sort%5Bby%5D=date&sort%5Bdirection%5D=asc"
          )
        ).withHeader("Authorization", equalTo("Bearer ABCDE"))
      )
    }
  }

  @Nested
  inner class `Find a single movement by ID` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/arrivals/testId")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/arrivals/testId")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `requires uuid`() {
      webTestClient.get().uri("/arrivals/")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `handles 404`() {
      basmApiMockServer.stubGetMovement("does-not-exist", 404, null)
      webTestClient.get().uri("/arrivals/does-not-exist")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isNotFound
        .expectBody().jsonPath("userMessage").isEqualTo("Resource not found")
    }

    @Test
    fun `returns json in expected formats`() {
      basmApiMockServer.stubGetMovement("testId", 200)
      webTestClient.get().uri("/arrivals/testId")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("arrival".loadJson(this))
    }
  }

  @Nested
  @DisplayName("Confirm arrivals tests")
  inner class ConfirmArrivalTests {
    val VALID_REQUEST = """
        {
          "firstName": "Alpha",
          "lastName": "Omega",
          "dateOfBirth": "1961-05-29",
          "gender": "M",
          "prisonId": "NMI",
          "movementReasonCode": "N",
          "imprisonmentStatus": "SENT03"
        }
      """

    @Test
    fun `create and book happy path`() {
      val offenderNo = "AA1111A"

      prisonApiMockServer.stubCreateOffender(offenderNo)
      prisonApiMockServer.stubAdmitOnNewBooking(offenderNo)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"), scopes = listOf("read", "write")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("offenderNo").isEqualTo(offenderNo)
    }

    @Test
    fun `create and book passes through auth token`() {
      val offenderNo = "AA1111A"

      prisonApiMockServer.stubCreateOffender(offenderNo)
      prisonApiMockServer.stubAdmitOnNewBooking(offenderNo)

      val token = getAuthorisation(roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"), scopes = listOf("read", "write"))

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .withBearerToken(token)
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("offenderNo").isEqualTo(offenderNo)

      prisonApiMockServer.verify(
        postRequestedFor(
          urlEqualTo(
            "/api/offenders"
          )
        ).withHeader("Authorization", equalTo(token))
      )
    }

    @Test
    fun `create and book request validation failure`() {
      val offenderNo = "AA1111A"

      prisonApiMockServer.stubCreateOffender(offenderNo)
      prisonApiMockServer.stubAdmitOnNewBooking(offenderNo)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"), scopes = listOf("read", "write")))
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
          """.trimIndent()
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
          """.trimIndent()
        )
    }

    @Test
    fun `create and book - prison-api create offender fails`() {

      prisonApiMockServer.stubCreateOffenderFails(500)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"), scopes = listOf("read", "write")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
    }

    @Test
    fun `create and book - prison-api create offender fails on a client error`() {

      prisonApiMockServer.stubCreateOffenderFails(400)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"), scopes = listOf("read", "write")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody()
    }

    @Test
    fun `create and book - prison-api create booking fails`() {
      val offenderNo = "AA1111A"

      prisonApiMockServer.stubCreateOffender(offenderNo)
      prisonApiMockServer.stubAdmitOnNewBookingFails(offenderNo, 500)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"), scopes = listOf("read", "write")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
    }
  }
}
