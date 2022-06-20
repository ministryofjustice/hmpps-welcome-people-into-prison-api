package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ArrivalType
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
    fun `tokens are cached`() {
      prisonerSearchMockServer.stubMatchPrisoners(200)
      prisonerSearchMockServer.stubMatchPrisonerByNameAndDateOfBirthOneResult()
      basmApiMockServer.stubGetPrison(200)
      basmApiMockServer.stubGetMovements(200)

      webTestClient.get().uri("/prisons/MDI/arrivals?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      webTestClient.get().uri("/prisons/MDI/arrivals?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      basmApiMockServer.verify(
        1,
        postRequestedFor(urlEqualTo("/oauth/token"))
      )
    }

    @Test
    fun `returns json in expected format`() {
      prisonerSearchMockServer.stubMatchPrisoners(200)
      prisonerSearchMockServer.stubMatchPrisonerByNameAndDateOfBirthOneResult()
      basmApiMockServer.stubGetPrison(200)
      basmApiMockServer.stubGetMovements(200)

      webTestClient.get().uri("/prisons/MDI/arrivals?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("arrivals".loadJson(this))
    }

    @Test
    fun `handles missing DoB`() {
      prisonerSearchMockServer.stubMatchPrisoners(200)
      basmApiMockServer.stubGetPrison(200)
      basmApiMockServer.stubGetMovements(200, hasMissingDob = true)

      webTestClient.get().uri("/prisons/MDI/arrivals?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("arrivals-with-missing-dob".loadJson(this))
    }

    @Test
    fun `calls service method with correct args`() {
      prisonerSearchMockServer.stubMatchPrisoners(200)
      prisonerSearchMockServer.stubMatchPrisonerByNameAndDateOfBirthOneResult()

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
      prisonerSearchMockServer.stubMatchPrisonerByNameAndDateOfBirthOneResult()
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
          "sex": "M",
          "prisonId": "NMI",
          "movementReasonCode": "N",
          "imprisonmentStatus": "SENT03"
        }
      """

    @AfterEach
    fun afterEach() {
      confirmedArrivalRepository.deleteAll()
    }

    @Test
    fun `create and book happy path`() {
      val prisonNumber = "AA1111A"
      val location = "Reception"
      prisonApiMockServer.stubCreateOffender(prisonNumber)
      prisonApiMockServer.stubAdmitOnNewBooking(prisonNumber)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
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
      prisonApiMockServer.stubAdmitOnNewBooking(prisonNumber)

      val token = getAuthorisation(
        roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
        scopes = listOf("read", "write")
      )

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .withBearerToken(token)
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("prisonNumber").isEqualTo(prisonNumber)

      prisonApiMockServer.verify(
        postRequestedFor(
          urlEqualTo(
            "/api/offenders"
          )
        ).withHeader("Authorization", equalTo(token))
      )
    }

    @Test
    fun `create and book records confirmed arrival`() {
      val prisonNumber = "AA1111A"
      val username = "USER_1"

      assertThat(confirmedArrivalRepository.count()).isEqualTo(0)
      prisonApiMockServer.stubCreateOffender(prisonNumber)
      prisonApiMockServer.stubAdmitOnNewBooking(prisonNumber)

      val token = getAuthorisation(
        username = username,
        roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
        scopes = listOf("read", "write")
      )

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .withBearerToken(token)
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().isOk
        .expectBody().jsonPath("prisonNumber").isEqualTo(prisonNumber)

      val confirmedArrival = confirmedArrivalRepository.findAll()[0]

      assertThat(confirmedArrival.username).isEqualTo(username)
      assertThat(confirmedArrival.prisonNumber).isEqualTo(prisonNumber)
      assertThat(confirmedArrival.arrivalType).isEqualTo(ArrivalType.NEW_TO_PRISON)
    }

    @Test
    fun `create and book request validation failure`() {
      val prisonNumber = "AA1111A"

      prisonApiMockServer.stubCreateOffender(prisonNumber)
      prisonApiMockServer.stubAdmitOnNewBooking(prisonNumber)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write")
          )
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
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
    }

    @Test
    fun `create and book - prison-api create offender fails on a client error without proper error response`() {

      prisonApiMockServer.stubCreateOffenderFails(400)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
    }

    @Test
    fun `create and book - prison-api create booking fails`() {
      val prisonNumber = "AA1111A"

      prisonApiMockServer.stubCreateOffender(prisonNumber)
      prisonApiMockServer.stubAdmitOnNewBookingFails(prisonNumber, 500)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is5xxServerError
        .expectBody()
    }

    @Test
    fun `create and book - prison-api create booking fails because of no capacity`() {
      val prisonNumber = "AA1111A"

      prisonApiMockServer.stubCreateOffender(prisonNumber)
      prisonApiMockServer.stubAdmitOnNewBookingFailsNoCapacity(prisonNumber)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody().json(
          """
        {
          "status": 409,
          "errorCode": "NO_CELL_CAPACITY",
          "moreInfo": null
        }
          """.trimIndent()
        )
    }

    @Test
    fun `create and book - prison-api create booking fails because prisoner already exist`() {
      val prisonNumber = "AA1111A"

      prisonApiMockServer.stubCreateOffenderFailsPrisonerAlreadyExist()
      prisonApiMockServer.stubAdmitOnNewBooking(prisonNumber)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody().json(
          """
        {
          "status": 400,
          "errorCode": "PRISONER_ALREADY_EXIST",
          "moreInfo": null
        }
          """.trimIndent()
        )
    }

    @Test
    fun `create and book - prison-api create booking fails because prisoner already exist - no ErrorCode due to old PrisonApi`() {
      val prisonNumber = "AA1111A"

      prisonApiMockServer.stubCreateOffenderFailsPrisonerAlreadyExistWithoutErrorCode()
      prisonApiMockServer.stubAdmitOnNewBooking(prisonNumber)

      webTestClient
        .post()
        .uri("/arrivals/06274b73-6aa9-490e-ab0e-2a25b3638068/confirm")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_BOOKING_CREATE", "ROLE_TRANSFER_PRISONER"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().is4xxClientError
        .expectBody().json(
          """
        {
          "status": 400,
          "moreInfo": null
        }
          """.trimIndent()
        )
    }
  }
}
