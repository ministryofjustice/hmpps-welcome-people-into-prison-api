package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.TemporaryAbsence
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.utils.LoadJsonHelper.Companion.loadJson
import java.time.LocalDate

@Suppress("ClassName")
class TemporaryAbsencesResourceTest : IntegrationTestBase() {
  @MockBean
  private lateinit var prisonService: PrisonService

  @Nested
  inner class `Get Temporary absence` {

    @Test
    fun `returns json in expected format`() {
      whenever(prisonService.getTemporaryAbsences("MDI")).thenReturn(
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
        .headers(setAuthorisation(roles = listOf("ROLE_SYSTEM_USER"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("temporaryAbsence".loadJson(this))
    }

    @Test
    fun `calls service method with correct args`() {
      webTestClient.get().uri("/temporary-absences/MDI")
        .headers(setAuthorisation(roles = listOf("ROLE_SYSTEM_USER"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      verify(prisonService).getTemporaryAbsences("MDI")
    }
  }
}
