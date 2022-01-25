package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.stream.Stream

class ArrivalsServiceConfirmArrivalTest {
  private val prisonService: PrisonService = mock()
  private val basmService: BasmService = mock()
  private val prisonerSearchService: PrisonerSearchService = mock()
  private val confirmedArrivalService: ConfirmedArrivalService = mock()

  private val arrivalsService = ArrivalsService(
    basmService, prisonService, prisonerSearchService, confirmedArrivalService, FIXED_CLOCK
  )

  @Test
  fun `Confirm arrival not known to NOMIS`() {

    whenever(basmService.getArrival(any())).thenReturn(ARRIVAL_PROTOTYPE.copy())
    whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(emptyList())
    whenever(prisonService.createOffender(any())).thenReturn(OFFENDER_NO)
    whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(BOOKING_ID)

    val response = arrivalsService.confirmArrival(MOVE_ID, CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE)

    assertThat(response.offenderNo).isEqualTo(OFFENDER_NO)

    verify(basmService).getArrival(MOVE_ID)

    inOrder(prisonService) {
      verify(prisonService).createOffender(CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE)
      verify(prisonService).admitOffenderOnNewBooking(OFFENDER_NO, CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE)
    }

    verify(confirmedArrivalService).add(
      MOVE_ID,
      OFFENDER_NO,
      PRISON_ID,
      BOOKING_ID,
      BOOKING_IN_TIME.toLocalDate(),
      ArrivalType.NEW_TO_PRISON
    )
  }

  @Test
  fun `Confirm arrival matched to NOMIS offender who is not in custody - with booking in time`() {
    doParameterizedConfirmArrivalTest(
      "",
      ArrivalType.NEW_BOOKING_EXISTING_OFFENDER,
      BOOKING_IN_TIME,
      { whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(BOOKING_ID) },
      { confirmArrivalDetail -> verify(prisonService).admitOffenderOnNewBooking(OFFENDER_NO, confirmArrivalDetail) }
    )
  }

  @Test
  fun `Confirm arrival matched to NOMIS offender who is in custody and is a non-court transfer, is rejected`() {
    whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(
      listOf(
        MatchPrisonerResponse(prisonerNumber = OFFENDER_NO, pncNumber = null, status = "ACTIVE IN")
      )
    )

    whenever(basmService.getArrival(any())).thenReturn(
      ARRIVAL_PROTOTYPE.copy(
        prisonNumber = OFFENDER_NO,
        isCurrentPrisoner = true,
        fromLocationType = LocationType.PRISON
      )
    )

    assertThatThrownBy {
      arrivalsService.confirmArrival(MOVE_ID, CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE)
    }.isInstanceOf(IllegalArgumentException::class.java)

    verify(basmService).getArrival(MOVE_ID)
  }

  @Test
  fun `Confirm arrival of court transfer for prisoner who is already in custody, calls prison service with correct args`() {

    whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(
      listOf(
        MatchPrisonerResponse(prisonerNumber = OFFENDER_NO, pncNumber = null, status = "ACTIVE IN")
      )
    )

    whenever(basmService.getArrival(any())).thenReturn(
      ARRIVAL_PROTOTYPE.copy(
        prisonNumber = OFFENDER_NO,
        isCurrentPrisoner = true,
      )
    )
    whenever(prisonService.returnFromCourt(any(), any())).thenReturn(BOOKING_ID)

    arrivalsService.confirmReturnFromCourt(
      MOVE_ID,
      CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE
    )

    verify(
      prisonService
    ).returnFromCourt(
      CONFIRM_COURT_RETURN_REQUEST_PROTOTYPE,
      ARRIVAL_PROTOTYPE.copy(
        prisonNumber = OFFENDER_NO,
        isCurrentPrisoner = true,
      )
    )
  }

  @Test
  fun `Confirm arrival of court transfer for prisoner who is already in custody, records the arrival as an event`() {

    whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(
      listOf(
        MatchPrisonerResponse(
          prisonerNumber = OFFENDER_NO, pncNumber = null, status = "ACTIVE IN"
        )
      )
    )

    whenever(basmService.getArrival(any())).thenReturn(
      ARRIVAL_PROTOTYPE.copy(
        prisonNumber = OFFENDER_NO,
        isCurrentPrisoner = true,
      )
    )
    whenever(prisonService.returnFromCourt(any(), any())).thenReturn(BOOKING_ID)

    arrivalsService.confirmReturnFromCourt(
      MOVE_ID,
      CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE
    )

    verify(
      confirmedArrivalService
    ).add(
      MOVE_ID,
      OFFENDER_NO,
      CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.prisonId!!,
      BOOKING_ID,
      LocalDate.now(FIXED_CLOCK),
      ArrivalType.COURT_TRANSFER
    )
  }

  @Test
  fun `Confirm arrival matched to NOMIS offender who is in custody is rejected - default arrival time`() {

    whenever(basmService.getArrival(any())).thenReturn(
      ARRIVAL_PROTOTYPE.copy(
        prisonNumber = OFFENDER_NO,
        isCurrentPrisoner = true,
        fromLocationType = LocationType.PRISON
      )
    )
    whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(
      listOf(
        MatchPrisonerResponse(
          prisonerNumber = OFFENDER_NO, pncNumber = null, status = "ACTIVE IN"
        )
      )
    )

    assertThatThrownBy {
      arrivalsService.confirmReturnFromCourt(
        MOVE_ID,
        CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE
      )
    }.isInstanceOf(IllegalArgumentException::class.java)

    verify(basmService).getArrival(MOVE_ID)
  }

  @ParameterizedTest
  @MethodSource("recallMovementReasonCodes")
  fun `Person having a Prison Number should be recalled when specified by movementReasonCode`(movementReasonCode: String) {
    doParameterizedConfirmArrivalTest(
      movementReasonCode,
      ArrivalType.RECALL,
      null,
      { whenever(prisonService.recallOffender(any(), any())).thenReturn(BOOKING_ID) },
      { confirmArrivalDetail -> verify(prisonService).recallOffender(OFFENDER_NO, confirmArrivalDetail) }
    )
  }

  @ParameterizedTest
  @MethodSource("nonRecallMovementReasonCodes")
  fun `Person having a Prison Number should be admitted on a new booking when not recalled`(movementReasonCode: String) {
    doParameterizedConfirmArrivalTest(
      movementReasonCode,
      ArrivalType.NEW_BOOKING_EXISTING_OFFENDER,
      null,
      { whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(BOOKING_ID) },
      { confirmArrivalDetail -> verify(prisonService).admitOffenderOnNewBooking(OFFENDER_NO, confirmArrivalDetail) }
    )
  }

  private fun doParameterizedConfirmArrivalTest(
    movementReasonCode: String,
    expectedArrivalType: ArrivalType,
    bookingInTime: LocalDateTime?,
    stubbing: () -> Unit,
    verification: (ConfirmArrivalDetail) -> Long
  ) {
    whenever(basmService.getArrival(any())).thenReturn(ARRIVAL_PROTOTYPE.copy(prisonNumber = OFFENDER_NO))
    whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(
      listOf(
        MatchPrisonerResponse(
          prisonerNumber = OFFENDER_NO, pncNumber = null, status = INACTIVE_OUT
        )
      )
    )
    stubbing()

    val confirmArrivalDetail = CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(
      bookingInTime = bookingInTime,
      movementReasonCode = movementReasonCode
    )

    val response = arrivalsService.confirmArrival(MOVE_ID, confirmArrivalDetail)

    assertThat(response.offenderNo).isEqualTo(OFFENDER_NO)

    verification(confirmArrivalDetail)

    verify(
      confirmedArrivalService
    ).add(
      MOVE_ID,
      OFFENDER_NO,
      PRISON_ID,
      BOOKING_ID,
      bookingInTime?.toLocalDate() ?: LocalDate.now(FIXED_CLOCK),
      expectedArrivalType
    )
  }

  companion object {
    private const val MOVE_ID = "beae6404-de16-406f-844a-7e043960d9ec"
    private const val OFFENDER_NO = "A1111AA"
    private const val BOOKING_ID = 1L
    private const val FIRST_NAME = "Eric"
    private const val LAST_NAME = "Bloodaxe"
    private val DATE_OF_BIRTH: LocalDate = LocalDate.of(1961, 4, 1)
    private val BOOKING_IN_TIME: LocalDateTime = LocalDateTime.of(2021, 10, 30, 13, 30)
    private const val GENDER = "M"
    private const val PRISON_ID = "NMI"

    private val FIXED_NOW: Instant = Instant.now()
    private val ZONE_ID: ZoneId = ZoneId.systemDefault()
    private val FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZONE_ID)

    val ARRIVAL_PROTOTYPE = Arrival(
      id = MOVE_ID,
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      prisonNumber = null,
      pncNumber = null,
      date = LocalDate.now(FIXED_CLOCK),
      fromLocation = "Kingston-upon-Hull Crown Court",
      fromLocationType = LocationType.COURT,
      isCurrentPrisoner = false
    )

    val CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE = ConfirmArrivalDetail(
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      gender = GENDER,
      prisonId = PRISON_ID,
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03",
      bookingInTime = BOOKING_IN_TIME,
      suffix = "",
      ethnicity = "",
      croNumber = "",
      fromLocationId = "",
      commentText = "",
      cellLocation = "",
    )
    val CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE = ConfirmCourtReturnRequest(
      prisonId = PRISON_ID,
    )

    val CONFIRM_COURT_RETURN_REQUEST_PROTOTYPE = ConfirmCourtReturnRequest(
      prisonId = PRISON_ID
    )

    @JvmStatic
    fun recallMovementReasonCodes(): Stream<String> = RECALL_MOVEMENT_REASON_CODES.stream()

    @JvmStatic
    fun nonRecallMovementReasonCodes(): Stream<String> =
      (ALL_MOVEMENT_REASON_CODES - RECALL_MOVEMENT_REASON_CODES).stream()
  }
}
