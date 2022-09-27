package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.config.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.AssignedLivingUnit
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsence
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsencesArrival
import java.time.LocalDate
import java.time.LocalDateTime

class TemporaryAbsenceServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()
  private val locationFormatter = LocationFormatter()
  private val arrivalListener: ArrivalListener = mock()
  private val temporaryAbsenceService = TemporaryAbsenceService(prisonApiClient, locationFormatter, arrivalListener)
  private val inmateDetail = InmateDetail(
    offenderNo = "G6081VQ", bookingId = 1L,
    assignedLivingUnit = AssignedLivingUnit(
      "NMI", 1, "RECP", "Nottingham (HMP)"
    )
  )

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

  @Test
  fun `confirm temporary absence`() {
    whenever(prisonApiClient.confirmTemporaryAbsencesArrival(any(), any())).thenReturn(inmateDetail)

    val request = ConfirmTemporaryAbsenceRequest(prisonId = "MDI", movementReasonCode = "C")
    assertThat(
      temporaryAbsenceService.confirmTemporaryAbsencesArrival("A1234AA", request)
    ).isEqualTo(ConfirmTemporaryAbsenceResponse(prisonNumber = "A1234AA", location = "Reception"))

    verify(prisonApiClient).confirmTemporaryAbsencesArrival("A1234AA", TemporaryAbsencesArrival("MDI", "C"))
  }

  @Test
  fun `confirm temporary absence recorded as arrival event`() {
    whenever(prisonApiClient.confirmTemporaryAbsencesArrival(any(), any())).thenReturn(inmateDetail)

    val request = ConfirmTemporaryAbsenceRequest(prisonId = "MDI", movementReasonCode = "C", arrivalId = "abc-123")
    assertThat(
      temporaryAbsenceService.confirmTemporaryAbsencesArrival("A1234AA", request)
    ).isEqualTo(ConfirmTemporaryAbsenceResponse(prisonNumber = "A1234AA", location = "Reception"))

    verify(arrivalListener).arrived(
      ArrivalEvent(
        arrivalId = "abc-123",
        prisonId = "MDI",
        prisonNumber = inmateDetail.offenderNo,
        arrivalType = ConfirmedArrivalType.TEMPORARY_ABSENCE,
        bookingId = inmateDetail.bookingId
      )
    )
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
