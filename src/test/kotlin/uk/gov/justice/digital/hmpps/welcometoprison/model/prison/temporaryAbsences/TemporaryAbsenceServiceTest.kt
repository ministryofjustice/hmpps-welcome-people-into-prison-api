package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsence
import java.time.LocalDate
import java.time.LocalDateTime

class TemporaryAbsenceServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()
  private val locationFormatter: LocationFormatter = mock()
  private val temporaryAbsenceService = TemporaryAbsenceService(prisonApiClient, locationFormatter)

  @Test
  fun getTemporaryAbsences() {
    whenever(prisonApiClient.getTemporaryAbsences(any())).thenReturn(listOf(arrivalKnownToNomis))

    val temporaryAbsences = temporaryAbsenceService.getTemporaryAbsences("MDI")

    assertThat(temporaryAbsences).containsExactly(
      TemporaryAbsenceResponse(
        firstName = "Jim",
        lastName = "Smith",
        dateOfBirth = LocalDate.of(1991, 7, 31),
        prisonNumber = "A1234AA",
        reasonForAbsence = "Hospital",
        movementDateTime = LocalDateTime.of(2022, 1, 18, 8, 0)
      )
    )

    verify(prisonApiClient).getTemporaryAbsences("MDI")
  }

  @Test
  fun getTemporaryAbsence() {
    whenever(prisonApiClient.getTemporaryAbsences(any())).thenReturn(listOf(arrivalKnownToNomis))

    val temporaryAbsence = temporaryAbsenceService.getTemporaryAbsence("MDI", "A1234AA")

    assertThat(temporaryAbsence).isEqualTo(
      TemporaryAbsenceResponse(
        firstName = "Jim",
        lastName = "Smith",
        dateOfBirth = LocalDate.of(1991, 7, 31),
        prisonNumber = "A1234AA",
        reasonForAbsence = "Hospital",
        movementDateTime = LocalDateTime.of(2022, 1, 18, 8, 0)
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
      offenderNo = "A1234AA",
      movementTime = LocalDateTime.of(2022, 1, 18, 8, 0),
      toAgency = "ABRYMC",
      toAgencyDescription = "Aberystwyth Magistrates Court",
      movementReasonCode = "RO",
      movementReason = "Hospital"
    )
  }
}
