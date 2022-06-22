package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalType.TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.AssignedLivingUnit
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.OffenderMovement
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferIn
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TransfersServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()
  private val prisonerSearchService: PrisonerSearchService = mock()
  private val locationFormatter: LocationFormatter = LocationFormatter()
  private val arrivalListener: ArrivalListener = mock()

  private val transfersService =
    TransfersService(prisonApiClient, prisonerSearchService, locationFormatter, arrivalListener)

  private val inmateDetail = InmateDetail(
    offenderNo = "G6081VQ", bookingId = 1L, agencyId = "NMI",
    assignedLivingUnit = AssignedLivingUnit(
      "NMI", 1, "RECP", "Nottingham (HMP)"
    )
  )

  @Test
  fun `getTransfers - happy path if prisoner has no PNC Number`() {
    whenever(prisonApiClient.getPrisonTransfersEnRoute(any())).thenReturn(listOf(arrivalKnownToNomis))
    whenever(prisonerSearchService.getPncNumbers(any())).thenReturn(mapOf("G6081VQ" to null))

    val transfers = transfersService.getTransfers("NMI")

    assertThat(transfers).containsExactly(
      Transfer(
        prisonNumber = "G6081VQ",
        dateOfBirth = LocalDate.of(1981, 7, 4),
        firstName = "Iruncekas",
        lastName = "Bronseria",
        fromLocation = "Doncaster (HMP)",
        date = LocalDate.of(2011, 9, 8),
        pncNumber = null,
      )
    )

    verify(prisonApiClient).getPrisonTransfersEnRoute("NMI")
    verify(prisonerSearchService).getPncNumbers(listOf("G6081VQ"))
  }

  @Test
  fun `getTransfers - happy path if prisoner has PNC Number`() {
    whenever(prisonApiClient.getPrisonTransfersEnRoute(any())).thenReturn(listOf(arrivalKnownToNomis))
    whenever(prisonerSearchService.getPncNumbers(any())).thenReturn(mapOf("G6081VQ" to "12/394773H"))

    val transfers = transfersService.getTransfers("NMI")

    assertThat(transfers).containsExactly(
      Transfer(
        prisonNumber = "G6081VQ",
        dateOfBirth = LocalDate.of(1981, 7, 4),
        firstName = "Iruncekas",
        lastName = "Bronseria",
        fromLocation = "Doncaster (HMP)",
        date = LocalDate.of(2011, 9, 8),
        pncNumber = "12/394773H",
      )
    )

    verify(prisonApiClient).getPrisonTransfersEnRoute("NMI")
    verify(prisonerSearchService).getPncNumbers(listOf("G6081VQ"))
  }

  @Test
  fun getTransfer() {
    whenever(prisonApiClient.getPrisonTransfersEnRoute(any())).thenReturn(listOf(arrivalKnownToNomis))

    val transfers = transfersService.getTransfer("NMI", "G6081VQ")

    assertThat(transfers).isEqualTo(
      Transfer(
        prisonNumber = "G6081VQ",
        dateOfBirth = LocalDate.of(1981, 7, 4),
        firstName = "Iruncekas",
        lastName = "Bronseria",
        fromLocation = "Doncaster (HMP)",
        date = LocalDate.of(2011, 9, 8),
        pncNumber = null,
      )
    )

    verify(prisonApiClient).getPrisonTransfersEnRoute("NMI")
  }

  @Test
  fun `getTransfer fail to be found`() {
    whenever(prisonApiClient.getPrisonTransfersEnRoute(any())).thenReturn(listOf(arrivalKnownToNomis))

    assertThatThrownBy {
      transfersService.getTransfer("NMI", "A1234AA")
    }.isEqualTo(NotFoundException("Could not find transfer with prisonNumber: 'A1234AA'"))
  }

  @Test
  fun `transfer-in offender`() {

    val transferInDetail = TransferInDetail(
      cellLocation = "MDI-RECP",
      commentText = "some transfer notes",
      receiveTime = LocalDateTime.now()
    )

    val transferIn = with(transferInDetail) { TransferIn(cellLocation, commentText, receiveTime) }

    val prisonNumber = "ABC123A"
    whenever(prisonApiClient.transferIn(any(), any())).thenReturn(inmateDetail)
    transfersService.transferInOffender(prisonNumber, transferInDetail)
    verify(prisonApiClient).transferIn(prisonNumber, transferIn)
  }

  @Test
  fun `transfer-in is recorded as an event`() {

    whenever(prisonApiClient.transferIn(any(), any())).thenReturn(inmateDetail)

    transfersService.transferInOffender(
      "ABC123A",
      TransferInDetail("MDI-RECP", "some transfer notes", LocalDateTime.now())
    )

    verify(arrivalListener).arrived(
      ArrivalEvent(
        prisonId = inmateDetail.agencyId,
        prisonNumber = inmateDetail.offenderNo,
        arrivalType = TRANSFER,
        bookingId = inmateDetail.bookingId
      )
    )
  }

  @Test
  fun `transfer-in offender throw exception when location is empty`() {

    val inmate = inmateDetail.copy(assignedLivingUnit = null, offenderNo = "G6081VQ")

    val transferInDetail = TransferInDetail(
      cellLocation = "MDI-RECP",
      commentText = "some transfer notes",
      receiveTime = LocalDateTime.now()
    )

    whenever(prisonApiClient.transferIn(any(), any())).thenReturn(inmate)
    assertThatThrownBy {
      transfersService.transferInOffender("G6081VQ", transferInDetail)
    }.hasMessage("Prisoner: 'G6081VQ' does not have assigned living unit")
  }

  companion object {
    private val arrivalKnownToNomis = OffenderMovement(
      offenderNo = "G6081VQ",
      bookingId = 472195,
      dateOfBirth = LocalDate.of(1981, 7, 4),
      firstName = "IRUNCEKAS",
      lastName = "BRONSERIA",
      fromAgency = "DNI",
      fromAgencyDescription = "Doncaster (HMP)",
      toAgency = "NMI",
      toAgencyDescription = "Nottingham (HMP)",
      movementType = "PRISON_REMAND",
      movementTypeDescription = "Transfers",
      movementReason = "NOTR",
      movementReasonDescription = "Normal Transfer",
      directionCode = "OUT",
      movementTime = LocalTime.of(12, 0, 0),
      movementDate = LocalDate.of(2011, 9, 8)
    )
  }
}
