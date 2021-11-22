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

class PrisonServiceTest {

  private val prisonApiClient = mock<PrisonApiClient>()
  private val prisonRegisterClient = mock<PrisonRegisterClient>()
  private val prisonService = PrisonService(prisonApiClient, prisonRegisterClient)

  private val prisonImage = "prisonImage".toByteArray()

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
    val confirmArrivalDetail = ConfirmArrivalDetail(
      firstName = "Fist",
      lastName = "Last",
      dateOfBirth = LocalDate.of(1978, 1, 1),
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
    val confirmArrivalDetail = ConfirmArrivalDetail(
      firstName = "Fist",
      lastName = "Last",
      dateOfBirth = LocalDate.of(1978, 1, 1),
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
