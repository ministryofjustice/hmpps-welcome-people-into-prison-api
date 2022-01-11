package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Gender
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.LocationType
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
  fun `gets user case loads`() {
    whenever(prisonApiClient.getUserCaseLoads()).thenReturn(
      listOf(
        UserCaseLoad(
          caseLoadId = "MDI",
          description = "Moorland Closed (HMP & YOI)"
        )
      )
    )

    val result: List<UserCaseLoad> = prisonService.getUserCaseLoads()

    assertThat(result).containsExactly(
      UserCaseLoad(
        caseLoadId = "MDI",
        description = "Moorland Closed (HMP & YOI)"
      )
    )
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

  @Test
  fun `transferInFromCourt calls prisonApi correctly and returns the bookingId`() {
    val confirmArrivalDetail = ConfirmArrivalDetail(
      firstName = "First",
      lastName = "Last",
      dateOfBirth = LocalDate.of(1978, 1, 1),
      gender = "M",
      prisonId = "MNI",
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03",
      bookingInTime = LocalDateTime.now(),
      commentText = " Some comments"
    )
    val arrival = Arrival(
      id = "0573de83-8a29-42aa-9ede-1068bc433fc5",
      firstName = "First",
      lastName = "last",
      dateOfBirth = LocalDate.of(1978, 1, 1),
      prisonNumber = "A1234AA",
      pncNumber = "01/1234X",
      date = LocalDate.now(),
      fromLocation = "Kingston-upon-Hull Crown Court",
      fromLocationType = LocationType.COURT,
      isCurrentPrisoner = true,
      gender = Gender.MALE
    )

    whenever(prisonApiClient.courtTransferIn(any(), any())).thenReturn(InmateDetail(1L))

    val result = prisonService.transferInFromCourt(confirmArrivalDetail, arrival)

    verify(prisonApiClient).courtTransferIn(
      arrival.prisonNumber!!,
      CourtTransferIn(
        confirmArrivalDetail.prisonId!!,
        confirmArrivalDetail.movementReasonCode,
        confirmArrivalDetail.commentText,
        confirmArrivalDetail.bookingInTime
      )
    )
    assertThat(result).isEqualTo(1L)
  }
}
