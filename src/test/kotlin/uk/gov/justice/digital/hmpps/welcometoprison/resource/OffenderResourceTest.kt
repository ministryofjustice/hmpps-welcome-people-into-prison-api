package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase

const val VALID_REQUEST = """
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

class OffenderResourceTest : IntegrationTestBase() {

  @Test
  fun `create and book happy path`() {
    val offenderNo = "AA1111A"

    prisonApiMockServer.stubCreateOffender(offenderNo)
    prisonApiMockServer.stubAdmitOnNewBooking(offenderNo)

    webTestClient
      .post()
      .uri("/offenders/create-and-book-into-prison")
      .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE"), scopes = listOf("read", "write")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(VALID_REQUEST)
      .exchange()
      .expectStatus().isOk
      .expectBody().jsonPath("offenderNo").isEqualTo(offenderNo)
  }

  @Test
  fun `create and book request validation failure`() {
    val offenderNo = "AA1111A"

    prisonApiMockServer.stubCreateOffender(offenderNo)
    prisonApiMockServer.stubAdmitOnNewBooking(offenderNo)

    webTestClient
      .post()
      .uri("/offenders/create-and-book-into-prison")
      .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE"), scopes = listOf("read", "write")))
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
      .uri("/offenders/create-and-book-into-prison")
      .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE"), scopes = listOf("read", "write")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(VALID_REQUEST)
      .exchange()
      .expectStatus().is5xxServerError
      .expectBody()
  }

  @Test
  fun `create and book - prison-api create booking fails`() {
    val offenderNo = "AA1111A"

    prisonApiMockServer.stubCreateOffender(offenderNo)
    prisonApiMockServer.stubAdmitOnNewBookingFails(offenderNo, 500)

    webTestClient
      .post()
      .uri("/offenders/create-and-book-into-prison")
      .headers(setAuthorisation(roles = listOf("ROLE_BOOKING_CREATE"), scopes = listOf("read", "write")))
      .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
      .bodyValue(VALID_REQUEST)
      .exchange()
      .expectStatus().is5xxServerError
      .expectBody()
  }
}
