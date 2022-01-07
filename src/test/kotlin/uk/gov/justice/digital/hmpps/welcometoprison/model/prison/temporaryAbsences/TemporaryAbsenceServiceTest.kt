package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryAbsences

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.TemporaryAbsence
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.TemporaryAbsenceService
import java.time.LocalDate

class TemporaryAbsenceServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()
  private val temporaryAbsenceService = TemporaryAbsenceService(prisonApiClient)

  @Test
  fun `getTemporaryAbsences`() {
    whenever(prisonApiClient.getTemporaryAbsences(any())).thenReturn(listOf(arrivalKnownToNomis))

    val temporaryAbsences = temporaryAbsenceService.getTemporaryAbsences("MDI")

    assertThat(temporaryAbsences).containsExactly(
      TemporaryAbsence(
        firstName = "Jim",
        lastName = "Smith",
        dateOfBirth = LocalDate.of(1991, 7, 31),
        prisonNumber = "A1234AA",
        reasonForAbsence = "Hospital"
      )
    )

    verify(prisonApiClient).getTemporaryAbsences("MDI")
  }

  @Test
  fun getTemporaryAbsence() {
    whenever(prisonApiClient.getTemporaryAbsences(any())).thenReturn(listOf(arrivalKnownToNomis))

    val temporaryAbsence = temporaryAbsenceService.getTemporaryAbsence("MDI", "A1234AA")

    assertThat(temporaryAbsence).isEqualTo(
      TemporaryAbsence(
        firstName = "Jim",
        lastName = "Smith",
        dateOfBirth = LocalDate.of(1991, 7, 31),
        prisonNumber = "A1234AA",
        reasonForAbsence = "Hospital"
      )
    )

    verify(prisonApiClient).getTemporaryAbsences("MDI")
  }

  @Test
  fun `getTemporaryAbsence fail to be found`() {
    whenever(prisonApiClient.getTemporaryAbsences(any())).thenReturn(listOf(arrivalKnownToNomis))

    assertThatThrownBy {
      temporaryAbsenceService.getTemporaryAbsence("MDI", "G6081VQ")
    }.isEqualTo(NotFoundException("Could not find temporary absence with prisonNumber: 'G6081VQ'"))
  }

  companion object {

    private val arrivalKnownToNomis = TemporaryAbsence(
      firstName = "Jim",
      lastName = "Smith",
      dateOfBirth = LocalDate.of(1991, 7, 31),
      prisonNumber = "A1234AA",
      reasonForAbsence = "Hospital"
    )
  }
}
