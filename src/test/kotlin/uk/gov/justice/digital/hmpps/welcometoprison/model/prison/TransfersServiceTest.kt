package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class TransfersServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()
  private val transfersService = TransfersService(prisonApiClient)

  @Test
  fun `getTransfers - happy path`() {
    whenever(prisonApiClient.getPrisonTransfersEnRoute(any())).thenReturn(listOf(arrivalKnownToNomis))

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

  @Test
  fun `transfer-in offender`() {

    val transferInDetail = TransferInDetail(
      cellLocation = "MDI-RECP",
      commentText = "some transfer notes",
      receiveTime = LocalDateTime.now()
    )

    val transferIn = with(transferInDetail) {
      TransferIn(
        cellLocation,
        commentText,
        receiveTime
      )
    }

    val prisonNumber = "ABC123A"
    transfersService.transferInOffender(prisonNumber, transferInDetail)
    verify(prisonApiClient).transferIn(prisonNumber, transferIn)
  }
}