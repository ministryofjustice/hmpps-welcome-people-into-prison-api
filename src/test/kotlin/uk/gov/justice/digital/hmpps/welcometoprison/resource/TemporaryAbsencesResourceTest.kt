package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.TemporaryAbsence
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.TemporaryAbsenceService
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson
import java.time.LocalDate
import java.time.LocalDateTime

@Suppress("ClassName")
class TemporaryAbsencesResourceTest : IntegrationTestBase() {
  @MockBean
  private lateinit var temporaryAbsenceService: TemporaryAbsenceService
  @MockBean
  private lateinit var temporaryAbsencesService: TemporaryAbsencesService

  @Nested
  inner class `Get Temporary absences` {

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/temporary-absences/MDI")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/temporary-absences/MDI")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      whenever(temporaryAbsenceService.getTemporaryAbsences("MDI")).thenReturn(
        listOf(
          TemporaryAbsence(
            firstName = "Jim",
            lastName = "Smith",
            dateOfBirth = LocalDate.of(1991, 7, 31),
            prisonNumber = "A1234AA",
            reasonForAbsence = "Hospital"
          ),
          TemporaryAbsence(
            firstName = "First",
            lastName = "Last",
            dateOfBirth = LocalDate.of(1980, 2, 23),
            prisonNumber = "A1278AA",
            reasonForAbsence = "Dentist"
          )
        )
      )
      webTestClient.get().uri("/temporary-absences/MDI")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("temporaryAbsences".loadJson(this))
    }

    @Test
    fun `calls service method with correct args`() {
      webTestClient.get().uri("/temporary-absences/MDI")
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
      webTestClient.get().uri("/temporary-absences/MDI/A1234AA")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/temporary-absences/MDI/A1234AA")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      whenever(temporaryAbsenceService.getTemporaryAbsence("MDI", "A1234AA")).thenReturn(
        TemporaryAbsence(
          firstName = "Jim",
          lastName = "Smith",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonNumber = "A1234AA",
          reasonForAbsence = "Hospital"
        )
      )
      webTestClient.get().uri("/temporary-absences/MDI/A1234AA")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("temporaryAbsence".loadJson(this))
    }

    @Test
    fun `calls service method with correct args`() {
      webTestClient.get().uri("/temporary-absences/MDI/A1234AA")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      verify(temporaryAbsenceService).getTemporaryAbsence("MDI", "A1234AA")
    }
  }

  @Nested
  inner class `Confirm arrival from temporary absence` {

    @Test
    fun `confirm arrival`() {
    whenever(temporaryAbsencesService.temporaryAbsencesArrival(eq("G5666UK"), any())).thenReturn(InmateDetail(1L))
      val token = getAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("write"))
      val temporaryAbsencesArrival = TemporaryAbsencesArrival(
        "NMI",
        "ET",
        "Comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      webTestClient
        .post()
        .uri("/temporary-absences/prisoner/G5666UK/temporary-absence-arrival")
        .bodyValue(temporaryAbsencesArrival)
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk
    }

  }

}
