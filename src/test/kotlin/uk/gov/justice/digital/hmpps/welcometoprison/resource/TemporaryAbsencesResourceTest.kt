package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.ConfirmTemporaryAbsenceRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.ConfirmTemporaryAbsenceResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.TemporaryAbsenceResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.TemporaryAbsenceService
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("ClassName")
class TemporaryAbsencesResourceTest : IntegrationTestBase() {
  @MockBean
  private lateinit var temporaryAbsenceService: TemporaryAbsenceService

  @Nested
  inner class `Get Temporary absences` {

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      whenever(temporaryAbsenceService.getTemporaryAbsences("MDI")).thenReturn(
        listOf(
          TemporaryAbsenceResponse(
            firstName = "Jim",
            lastName = "Smith",
            dateOfBirth = LocalDate.of(1991, 7, 31),
            prisonNumber = "A1234AA",
            reasonForAbsence = "Hospital",
            movementDateTime = LocalDateTime.of(2022, 1, 18, 8, 0)
          ),
          TemporaryAbsenceResponse(
            firstName = "First",
            lastName = "Last",
            dateOfBirth = LocalDate.of(1980, 2, 23),
            prisonNumber = "A1278AA",
            reasonForAbsence = "Dentist",
            movementDateTime = LocalDateTime.of(2022, 1, 20, 8, 0)
          )
        )
      )
      webTestClient.get().uri("/prison/MDI/temporary-absences")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("temporaryAbsences".loadJson(this))
    }

    @Test
    fun `calls service method with correct args`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      verify(temporaryAbsenceService).getTemporaryAbsences("MDI")
    }
  }

  @Nested
  inner class `Get Temporary absence` {

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences/A1234AA")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences/A1234AA")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      whenever(temporaryAbsenceService.getTemporaryAbsence("MDI", "A1234AA")).thenReturn(
        TemporaryAbsenceResponse(
          firstName = "Jim",
          lastName = "Smith",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonNumber = "A1234AA",
          reasonForAbsence = "Hospital",
          movementDateTime = LocalDateTime.of(2022, 1, 18, 8, 0)
        )
      )
      webTestClient.get().uri("/prison/MDI/temporary-absences/A1234AA")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("temporaryAbsence".loadJson(this))
    }

    @Test
    fun `calls service method with correct args`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences/A1234AA")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      verify(temporaryAbsenceService).getTemporaryAbsence("MDI", "A1234AA")
    }
  }

  @Nested
  inner class `Confirm arrival from temporary absence` {

    // TODO this test need to be refactor to end to end test please  follow ArrivalResourceTest

    @Test
    fun `confirm arrival`() {
      whenever(
        temporaryAbsenceService.confirmTemporaryAbsencesArrival(any(), any())
      ).thenReturn(ConfirmTemporaryAbsenceResponse("G5666UK", "Reception"))
      val token = getAuthorisation(roles = listOf("ROLE_TRANSFER_PRISONER"), scopes = listOf("write"))
      val confirmTemporaryAbsenceRequest = ConfirmTemporaryAbsenceRequest(
        "NMI",
        "ET",
        "Comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )
      webTestClient
        .post()
        .uri("/temporary-absences/G5666UK/confirm")
        .bodyValue(confirmTemporaryAbsenceRequest)
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("prisonNumber").isEqualTo("G5666UK")
        .jsonPath("location").isEqualTo("Reception")
      verify(temporaryAbsenceService).confirmTemporaryAbsencesArrival("G5666UK", confirmTemporaryAbsenceRequest)
    }

    @Test
    fun `requires authentication`() {
      val confirmTemporaryAbsenceRequest = ConfirmTemporaryAbsenceRequest(
        "NMI",
        "ET",
        "Comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      webTestClient.post().uri("/temporary-absences/G5666UK/confirm")
        .bodyValue(confirmTemporaryAbsenceRequest)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      val token = getAuthorisation(roles = listOf(), scopes = listOf("write"))
      val confirmTemporaryAbsenceRequest = ConfirmTemporaryAbsenceRequest(
        "NMI",
        "ET",
        "Comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      webTestClient.post().uri("/temporary-absences/G5666UK/confirm")
        .bodyValue(confirmTemporaryAbsenceRequest)
        .withBearerToken(token)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }
  }
}
