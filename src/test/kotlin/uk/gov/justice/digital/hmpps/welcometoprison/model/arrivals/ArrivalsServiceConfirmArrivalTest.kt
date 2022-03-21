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
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.AssignedLivingUnit
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerDetails
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
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
  private val locationFormatter: LocationFormatter = LocationFormatter()

  private val arrivalsService = ArrivalsService(
    basmService, prisonService, prisonerSearchService, confirmedArrivalService, locationFormatter, FIXED_CLOCK
  )

  @Test
  fun `Confirm arrival throw exception when location is empty`() {
    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PrisonerDetails(
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        dateOfBirth = DATE_OF_BIRTH,
        prisonNumber = PRISON_NUMBER,
        pncNumber = PNC_NUMBER,
        croNumber = "12/4321",
        isCurrentPrisoner = false,
        sex = GENDER
      )
    )
    whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(INMATE_DETAIL_NO_UNIT)
    assertThatThrownBy {
      arrivalsService.confirmArrival(MOVE_ID, CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE)
    }.hasMessage("Prisoner: '$PRISON_NUMBER' do not have assigned living unit")
  }

  @Test
  fun `Confirm arrival not known to NOMIS`() {
    whenever(prisonService.createOffender(any())).thenReturn(PRISON_NUMBER)
    whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(INMATE_DETAIL)

    val response = arrivalsService.confirmArrival(MOVE_ID, CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(prisonNumber = null))

    assertThat(response.prisonNumber).isEqualTo(PRISON_NUMBER)

    inOrder(prisonService) {
      verify(prisonService).createOffender(CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(prisonNumber = null))
      verify(prisonService).admitOffenderOnNewBooking(
        PRISON_NUMBER,
        CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(prisonNumber = null)
      )
    }

    verify(confirmedArrivalService).add(
      MOVE_ID,
      PRISON_NUMBER,
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
      { whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(INMATE_DETAIL) },
      { confirmArrivalDetail -> verify(prisonService).admitOffenderOnNewBooking(PRISON_NUMBER, confirmArrivalDetail) }
    )
  }

  @Test
  fun `Confirm arrival matched to NOMIS offender who is in custody and is a non-court transfer, is rejected`() {
    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PrisonerDetails(
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        dateOfBirth = DATE_OF_BIRTH,
        prisonNumber = PRISON_NUMBER,
        pncNumber = null,
        croNumber = CRO_NUMBER,
        isCurrentPrisoner = true,
        sex = GENDER
      )
    )

    whenever(basmService.getArrival(any())).thenReturn(
      ARRIVAL_PROTOTYPE.copy(
        prisonNumber = PRISON_NUMBER,
        isCurrentPrisoner = true,
        fromLocationType = LocationType.PRISON
      )
    )

    assertThatThrownBy {
      arrivalsService.confirmArrival(MOVE_ID, CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE)
    }.isInstanceOf(IllegalArgumentException::class.java)

    verify(prisonerSearchService).getPrisoner(PRISON_NUMBER)
  }

  @Test
  fun `Confirm arrival of court transfer for prisoner who is already in custody, calls prison service with correct args`() {

    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PrisonerDetails(
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        dateOfBirth = DATE_OF_BIRTH,
        prisonNumber = PRISON_NUMBER,
        croNumber = CRO_NUMBER,
        pncNumber = null,
        isCurrentPrisoner = true,
        sex = GENDER
      )
    )

    whenever(basmService.getArrival(any())).thenReturn(
      ARRIVAL_PROTOTYPE.copy(
        prisonNumber = PRISON_NUMBER,
        isCurrentPrisoner = true,
      )
    )
    whenever(prisonService.returnFromCourt(any(), any())).thenReturn(CONFIRM_COURT_RETURN_RESPONSE)

    arrivalsService.confirmReturnFromCourt(
      MOVE_ID,
      CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE
    )

    verify(prisonService).returnFromCourt(PRISON_ID, PRISON_NUMBER)
  }

  @Test
  fun `Confirm arrival of court transfer for prisoner who is already in custody, records the arrival as an event`() {

    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PrisonerDetails(
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
        dateOfBirth = DATE_OF_BIRTH,
        prisonNumber = PRISON_NUMBER,
        pncNumber = null,
        croNumber = CRO_NUMBER,
        isCurrentPrisoner = true,
        sex = GENDER
      )
    )

    whenever(basmService.getArrival(any())).thenReturn(
      ARRIVAL_PROTOTYPE.copy(
        prisonNumber = PRISON_NUMBER,
        isCurrentPrisoner = true,
      )
    )
    whenever(prisonService.returnFromCourt(any(), any())).thenReturn(CONFIRM_COURT_RETURN_RESPONSE)

    arrivalsService.confirmReturnFromCourt(
      MOVE_ID,
      CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE
    )

    verify(
      confirmedArrivalService
    ).add(
      MOVE_ID,
      PRISON_NUMBER,
      CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.prisonId!!,
      BOOKING_ID,
      LocalDate.now(FIXED_CLOCK),
      ArrivalType.COURT_TRANSFER
    )
  }

  @ParameterizedTest
  @MethodSource("recallMovementReasonCodes")
  fun `Person having a Prison Number should be recalled when specified by movementReasonCode`(movementReasonCode: String) {
    doParameterizedConfirmArrivalTest(
      movementReasonCode,
      ArrivalType.RECALL,
      null,
      { whenever(prisonService.recallOffender(any(), any())).thenReturn(INMATE_DETAIL) },
      { confirmArrivalDetail -> verify(prisonService).recallOffender(PRISON_NUMBER, confirmArrivalDetail) }
    )
  }

  @ParameterizedTest
  @MethodSource("nonRecallMovementReasonCodes")
  fun `Person having a Prison Number should be admitted on a new booking when not recalled`(movementReasonCode: String) {
    doParameterizedConfirmArrivalTest(
      movementReasonCode,
      ArrivalType.NEW_BOOKING_EXISTING_OFFENDER,
      null,
      { whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(INMATE_DETAIL) },
      { confirmArrivalDetail -> verify(prisonService).admitOffenderOnNewBooking(PRISON_NUMBER, confirmArrivalDetail) }
    )
  }

  private fun doParameterizedConfirmArrivalTest(
    movementReasonCode: String,
    expectedArrivalType: ArrivalType,
    bookingInTime: LocalDateTime?,
    stubbing: () -> Unit,
    verification: (ConfirmArrivalDetail) -> InmateDetail
  ) {
    whenever(basmService.getArrival(any())).thenReturn(ARRIVAL_PROTOTYPE.copy(prisonNumber = PRISON_NUMBER))
    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PrisonerDetails(
        firstName = FIRST_NAME, lastName = LAST_NAME, dateOfBirth = DATE_OF_BIRTH,
        prisonNumber = PRISON_NUMBER, pncNumber = null, croNumber = CRO_NUMBER,
        isCurrentPrisoner = false, sex = GENDER
      )
    )
    stubbing()

    val confirmArrivalDetail = CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(
      bookingInTime = bookingInTime,
      movementReasonCode = movementReasonCode
    )

    val response = arrivalsService.confirmArrival(MOVE_ID, confirmArrivalDetail)

    assertThat(response.prisonNumber).isEqualTo(PRISON_NUMBER)

    verification(confirmArrivalDetail)

    verify(
      confirmedArrivalService
    ).add(
      MOVE_ID,
      PRISON_NUMBER,
      PRISON_ID,
      BOOKING_ID,
      bookingInTime?.toLocalDate() ?: LocalDate.now(FIXED_CLOCK),
      expectedArrivalType
    )
  }

  companion object {
    private const val MOVE_ID = "beae6404-de16-406f-844a-7e043960d9ec"
    private const val PRISON_NUMBER = "A1111AA"
    private const val CRO_NUMBER = "12/4321"
    private const val PNC_NUMBER = "01/123456"
    private const val BOOKING_ID = 1L
    private const val FIRST_NAME = "Eric"
    private const val LAST_NAME = "Bloodaxe"
    private val DATE_OF_BIRTH: LocalDate = LocalDate.of(1961, 4, 1)
    private val BOOKING_IN_TIME: LocalDateTime = LocalDateTime.of(2021, 10, 30, 13, 30)
    private const val GENDER = "M"
    private const val PRISON_ID = "NMI"
    private const val LOCATION_ID = 1

    private val FIXED_NOW: Instant = Instant.now()
    private val ZONE_ID: ZoneId = ZoneId.systemDefault()
    private val FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZONE_ID)
    private val LOCATION = "RECP"
    private val INMATE_DETAIL = InmateDetail(
      offenderNo = PRISON_NUMBER, bookingId = BOOKING_ID,
      assignedLivingUnit = AssignedLivingUnit(
        PRISON_ID, LOCATION_ID, LOCATION, "Nottingham (HMP)"
      )
    )
    private val INMATE_DETAIL_NO_UNIT = InmateDetail(
      offenderNo = PRISON_NUMBER, bookingId = BOOKING_ID,
    )
    private val CONFIRM_COURT_RETURN_RESPONSE =
      ConfirmCourtReturnResponse(prisonNumber = PRISON_NUMBER, location = LOCATION, bookingId = BOOKING_ID)
    val ARRIVAL_PROTOTYPE = Arrival(
      id = MOVE_ID,
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      prisonNumber = PRISON_NUMBER,
      pncNumber = null,
      date = LocalDate.now(FIXED_CLOCK),
      fromLocation = "Kingston-upon-Hull Crown Court",
      fromLocationId = "HULLCC",
      fromLocationType = LocationType.COURT,
      isCurrentPrisoner = false,
    )

    val CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE = ConfirmArrivalDetail(
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      sex = GENDER,
      prisonId = PRISON_ID,
      prisonNumber = PRISON_NUMBER,
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
      prisonNumber = PRISON_NUMBER
    )

    @JvmStatic
    fun recallMovementReasonCodes(): Stream<String> = RECALL_MOVEMENT_REASON_CODES.stream()

    @JvmStatic
    fun nonRecallMovementReasonCodes(): Stream<String> =
      (ALL_MOVEMENT_REASON_CODES - RECALL_MOVEMENT_REASON_CODES).stream()
  }
}
