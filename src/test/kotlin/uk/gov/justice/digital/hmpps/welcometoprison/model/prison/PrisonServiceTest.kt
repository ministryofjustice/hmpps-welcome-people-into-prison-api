package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.ClientException
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import java.time.LocalDate

class PrisonServiceTest {

  private val prisonApiClient = mock<PrisonApiClient>()
  private val prisonRegisterClient = mock<PrisonRegisterClient>()
  private val locationFormatter: LocationFormatter = LocationFormatter()
  private val prisonService = PrisonService(prisonApiClient, prisonRegisterClient, locationFormatter)

  private val prisonImage = "prisonImage".toByteArray()
  private val inmateDetailNewBooking = InmateDetail(
    offenderNo = "ABC123A",
    bookingId = 1L
  )
  private val inmateDetail = InmateDetail(
    offenderNo = "ABC123A",
    bookingId = 1L,
    assignedLivingUnit = AssignedLivingUnit(
      "134",
      1,
      "RECP",
      "Nottingham (HMP)"
    )
  )

  @Test
  fun `gets prisoner image`() {
    whenever(prisonApiClient.getPrisonerImage(any())).thenReturn(prisonImage)

    val result = prisonService.getPrisonerImage("A12345")

    assertThat(result).isEqualTo(prisonImage)
  }

  @Test
  fun `throw ClientException when prisoner image not exist`() {
    whenever(prisonApiClient.getPrisonerImage(any())).thenThrow(ClientException::class.java)

    assertThatThrownBy {
      prisonApiClient.getPrisonerImage("A12345")
    }.isInstanceOf(ClientException::class.java)
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
      sex = "M",
      prisonId = "MNI",
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03",
    )
    val admitOnNewBookingDetail = with(confirmArrivalDetail) {
      AdmitOnNewBookingDetail(
        prisonId = prisonId!!,
        fromLocationId = fromLocationId,
        movementReasonCode = movementReasonCode!!,
        youthOffender = youthOffender,
        imprisonmentStatus = imprisonmentStatus!!
      )
    }
    val prisonNumber = "ABC123A"
    val expectedBookingId = 1L
    whenever(prisonApiClient.admitOffenderOnNewBooking(any(), any())).thenReturn(inmateDetailNewBooking)
    val result = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmArrivalDetail)
    verify(prisonApiClient).admitOffenderOnNewBooking(prisonNumber, admitOnNewBookingDetail)
    assertThat(result.bookingId).isEqualTo(expectedBookingId)
  }

  @Test
  fun `recall Offender`() {
    val confirmArrivalDetail = ConfirmArrivalDetail(
      firstName = "Fist",
      lastName = "Last",
      dateOfBirth = LocalDate.of(1978, 1, 1),
      sex = "M",
      prisonId = "MNI",
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03",
    )
    val recallBooking = with(confirmArrivalDetail) {
      RecallBooking(
        prisonId = prisonId!!,
        fromLocationId = fromLocationId,
        movementReasonCode = movementReasonCode!!,
        youthOffender = youthOffender,
        imprisonmentStatus = imprisonmentStatus!!
      )
    }
    val prisonNumber = "ABC123A"
    val expectedBookingId = 1L
    whenever(prisonApiClient.recallOffender(any(), any())).thenReturn(inmateDetail)
    val result = prisonService.recallOffender(prisonNumber, confirmArrivalDetail)
    verify(prisonApiClient).recallOffender(prisonNumber, recallBooking)
    assertThat(result.bookingId).isEqualTo(expectedBookingId)
  }

  @Test
  fun `transferInFromCourt calls prisonApi correctly and returns the bookingId`() {
    val expectedBookingId = 1L

    whenever(prisonApiClient.courtTransferIn(any(), any())).thenReturn(inmateDetail)

    val result = prisonService.returnFromCourt("MNI", "A1234AA")

    verify(prisonApiClient).courtTransferIn("A1234AA", CourtTransferIn("MNI"))
    assertThat(result.bookingId).isEqualTo(expectedBookingId)
  }
}
