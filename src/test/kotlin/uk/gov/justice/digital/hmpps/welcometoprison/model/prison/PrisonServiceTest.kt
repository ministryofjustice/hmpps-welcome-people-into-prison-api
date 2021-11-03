package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.LocationType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class PrisonServiceTest {

  private val prisonApiClient = mock<PrisonApiClient>()
  private val prisonRegisterClient = mock<PrisonRegisterClient>()
  private val prisonService = PrisonService(prisonApiClient, prisonRegisterClient)

  private val prisonImage = "prisonImage".toByteArray()

  @Test
  fun `get transfers`() {
    whenever(prisonApiClient.getPrisonTransfersEnRoute(any())).thenReturn(
      listOf(
        OffenderMovement(
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
        ),
      )
    )

    val moves = prisonService.getTransfers("NMI", LocalDate.now())

    assertThat(moves).containsExactly(
      Arrival(
        id = null,
        prisonNumber = "G6081VQ",
        dateOfBirth = LocalDate.of(1981, 7, 4),
        firstName = "Iruncekas",
        lastName = "Bronseria",
        fromLocation = "Doncaster (HMP)",
        fromLocationType = LocationType.PRISON,
        date = LocalDate.of(2011, 9, 8),
        pncNumber = null,
        isCurrentPrisoner = true
      )
    )

    verify(prisonApiClient).getPrisonTransfersEnRoute("NMI")
  }

  @Test
  fun `gets prisoner image`() {
    whenever(prisonApiClient.getPrisonerImage(any())).thenReturn(prisonImage)

    val result = prisonService.getPrisonerImage("A12345")

    assertThat(result).isEqualTo(prisonImage)
  }

  @Test
  fun `get prison`() {
    whenever(prisonRegisterClient.getPrison(any())).thenReturn(Prison("Some description"))

    val result = prisonService.getPrison("MDI")

    assertThat(result).isEqualTo(Prison("Some description"))
  }

  @Test
  fun `get prison not found`() {
    whenever(prisonApiClient.getAgency(any())).thenReturn(null)

    assertThatThrownBy { prisonService.getPrison("MDI") }.isInstanceOf(NotFoundException::class.java)
  }

  @Test
  fun `admit Offender On New Booking`() {
    val confirmArrivalDetail =  ConfirmArrivalDetail(
      firstName = "Fist",
      lastName = "Last",
      dateOfBirth = LocalDate.of(1978,1,1),
      gender = "M",
      prisonId = "MNI",
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03",
      bookingInTime = LocalDateTime.now()
    )
    val admitOnNewBookingDetail = with(confirmArrivalDetail) {
      AdmitOnNewBookingDetail(
        prisonId = prisonId!!,
        bookingInTime,
        fromLocationId,
        movementReasonCode = movementReasonCode!!,
        youthOffender,
        cellLocation,
        imprisonmentStatus = imprisonmentStatus!!
      )
    }
    val prisonNumber = "ABC123A"
    val expectedBookingId = 1L
    whenever(prisonApiClient.admitOffenderOnNewBooking(any(), any())).thenReturn(InmateDetail(expectedBookingId))
    val result = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmArrivalDetail)
    verify(prisonApiClient).admitOffenderOnNewBooking(prisonNumber, admitOnNewBookingDetail)
    assertThat(result).isEqualTo(expectedBookingId)
  }

  @Test
  fun `recall Offender`() {
    val confirmArrivalDetail =  ConfirmArrivalDetail(
      firstName = "Fist",
      lastName = "Last",
      dateOfBirth = LocalDate.of(1978,1,1),
      gender = "M",
      prisonId = "MNI",
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03",
      bookingInTime = LocalDateTime.now()
    )
    val recallBooking = with(confirmArrivalDetail) {
      RecallBooking(
        prisonId = prisonId!!,
        bookingInTime,
        fromLocationId,
        movementReasonCode = movementReasonCode!!,
        youthOffender,
        cellLocation,
        imprisonmentStatus = imprisonmentStatus!!
      )
    }
    val prisonNumber = "ABC123A"
    val expectedBookingId = 1L
    whenever(prisonApiClient.recallOffender(any(), any())).thenReturn(InmateDetail(expectedBookingId))
    val result = prisonService.recallOffender(prisonNumber, confirmArrivalDetail)
    verify(prisonApiClient).recallOffender(prisonNumber, recallBooking)
    assertThat(result).isEqualTo(expectedBookingId)
  }


}
