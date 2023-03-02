package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.mockito.kotlin.refEq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.config.ConflictException
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.AssignedLivingUnit
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerDetails
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import java.time.LocalDate
import java.util.stream.Stream

class ConfirmationServiceTest {
  private val prisonService: PrisonService = mock()
  private val prisonerSearchService: PrisonerSearchService = mock()
  private val arrivalListener: ArrivalListener = mock()
  private val locationFormatter: LocationFormatter = LocationFormatter()

  private val confirmationService = ConfirmationService(
    prisonService,
    prisonerSearchService,
    arrivalListener,
    locationFormatter,
  )

  @Test
  fun `Confirm arrival throw exception when location is empty`() {
    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(PRISONER_DETAILS_PROTOTYPE)
    whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(INMATE_DETAIL_NO_UNIT)
    assertThatThrownBy {
      confirmationService.confirmArrival(Confirmation.Expected(ARRIVAL_ID, CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE))
    }.hasMessage("Prisoner: '$PRISON_NUMBER' does not have assigned living unit")
  }

  @Test
  fun `Confirm arrival not known to NOMIS`() {
    whenever(prisonService.createOffender(any())).thenReturn(INMATE_DETAIL)

    val response =
      confirmationService.confirmArrival(
        Confirmation.Expected(
          ARRIVAL_ID,
          CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(prisonNumber = null),
        ),
      )

    assertThat(response.prisonNumber).isEqualTo(PRISON_NUMBER)

    inOrder(prisonService) {
      verify(prisonService).createOffender(CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(prisonNumber = null))
    }

    verify(arrivalListener).arrived(
      refEq(
        ArrivalEvent(
          arrivalId = ARRIVAL_ID,
          prisonNumber = PRISON_NUMBER,
          prisonId = PRISON_ID,
          bookingId = BOOKING_ID,
          arrivalType = ConfirmedArrivalType.NEW_TO_PRISON,
        ),
      ),
    )
  }

  @Test
  fun `Confirm arrival matched to NOMIS offender who is in custody and is a non-court transfer, is rejected`() {
    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PRISONER_DETAILS_PROTOTYPE.copy(isCurrentPrisoner = true),
    )

    assertThatThrownBy {
      confirmationService.confirmArrival(Confirmation.Expected(ARRIVAL_ID, CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE))
    }.isInstanceOf(ConflictException::class.java)

    verify(prisonerSearchService).getPrisoner(PRISON_NUMBER)
  }

  @Test
  fun `Confirm arrival of court transfer for prisoner who is already in custody, calls prison service with correct args`() {
    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PRISONER_DETAILS_PROTOTYPE.copy(isCurrentPrisoner = true),
    )

    whenever(prisonService.returnFromCourt(any(), any())).thenReturn(CONFIRM_COURT_RETURN_RESPONSE)

    confirmationService.confirmReturnFromCourt(
      ARRIVAL_ID,
      CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE,
    )

    verify(prisonService).returnFromCourt(PRISON_ID, PRISON_NUMBER)
  }

  @Test
  fun `Confirm arrival of court transfer for prisoner who is already in custody, records the arrival as an event`() {
    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PRISONER_DETAILS_PROTOTYPE.copy(isCurrentPrisoner = true),
    )

    whenever(prisonService.returnFromCourt(any(), any())).thenReturn(CONFIRM_COURT_RETURN_RESPONSE)

    confirmationService.confirmReturnFromCourt(
      ARRIVAL_ID,
      CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE,
    )

    verify(arrivalListener).arrived(
      refEq(
        ArrivalEvent(
          arrivalId = ARRIVAL_ID,
          prisonNumber = PRISON_NUMBER,
          prisonId = CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.prisonId!!,
          bookingId = BOOKING_ID,
          arrivalType = ConfirmedArrivalType.COURT_TRANSFER,
        ),
      ),
    )
  }

  @Test
  fun `Confirm arrival matched to NOMIS offender who is not in custody and is a non-court transfer, is rejected`() {
    whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
      PRISONER_DETAILS_PROTOTYPE.copy(isCurrentPrisoner = false),
    )

    assertThatThrownBy {
      confirmationService.confirmReturnFromCourt(
        ARRIVAL_ID,
        CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE,
      )
    }.isInstanceOf(ConflictException::class.java)

    verify(prisonerSearchService).getPrisoner(PRISON_NUMBER)
  }

  @Nested
  inner class ExpectedArrival {
    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ConfirmationServiceTest#recallMovementReasonCodes")
    fun `Person having a Prison Number should be recalled when specified by movementReasonCode`(movementReasonCode: String) {
      doParameterizedExpectedConfirmationTest(
        movementReasonCode,
        ConfirmedArrivalType.RECALL,
        { whenever(prisonService.recallOffender(any(), any())).thenReturn(INMATE_DETAIL) },
        { confirmation -> verify(prisonService).recallOffender(PRISON_NUMBER, confirmation.detail) },
      )
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ConfirmationServiceTest#nonRecallMovementReasonCodes")
    fun `Person having a Prison Number should be admitted on a new booking when not recalled`(movementReasonCode: String) {
      doParameterizedExpectedConfirmationTest(
        movementReasonCode,
        ConfirmedArrivalType.NEW_BOOKING_EXISTING_OFFENDER,
        { whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(INMATE_DETAIL) },
        { confirmation -> verify(prisonService).admitOffenderOnNewBooking(PRISON_NUMBER, confirmation.detail) },
      )
    }

    private fun doParameterizedExpectedConfirmationTest(
      movementReasonCode: String,
      expectedArrivalType: ConfirmedArrivalType,
      stubbing: () -> Unit,
      verification: (Confirmation) -> InmateDetail,
    ) {
      whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
        PRISONER_DETAILS_PROTOTYPE.copy(isCurrentPrisoner = false),
      )
      stubbing()

      val confirmation = Confirmation.Expected(
        ARRIVAL_ID,
        CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(
          movementReasonCode = movementReasonCode,
        ),
      )

      val response = confirmationService.confirmArrival(confirmation)

      assertThat(response.prisonNumber).isEqualTo(PRISON_NUMBER)

      verification(confirmation)

      verify(arrivalListener).arrived(
        refEq(
          ArrivalEvent(
            arrivalId = ARRIVAL_ID,
            prisonNumber = PRISON_NUMBER,
            prisonId = PRISON_ID,
            bookingId = BOOKING_ID,
            arrivalType = expectedArrivalType,
          ),
        ),
      )
    }
  }

  @Nested
  inner class UnexpectedArrival {
    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ConfirmationServiceTest#recallMovementReasonCodes")
    fun `Person having a Prison Number should be recalled when specified by movementReasonCode`(movementReasonCode: String) {
      doParameterizedUnexpectedConfirmationTest(
        movementReasonCode,
        ConfirmedArrivalType.RECALL,
        { whenever(prisonService.recallOffender(any(), any())).thenReturn(INMATE_DETAIL) },
        { confirmation -> verify(prisonService).recallOffender(PRISON_NUMBER, confirmation.detail) },
      )
    }

    @ParameterizedTest
    @MethodSource("uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ConfirmationServiceTest#nonRecallMovementReasonCodes")
    fun `Person having a Prison Number should be admitted on a new booking when not recalled`(movementReasonCode: String) {
      doParameterizedUnexpectedConfirmationTest(
        movementReasonCode,
        ConfirmedArrivalType.NEW_BOOKING_EXISTING_OFFENDER,
        { whenever(prisonService.admitOffenderOnNewBooking(any(), any())).thenReturn(INMATE_DETAIL) },
        { confirmation -> verify(prisonService).admitOffenderOnNewBooking(PRISON_NUMBER, confirmation.detail) },
      )
    }

    private fun doParameterizedUnexpectedConfirmationTest(
      movementReasonCode: String,
      expectedArrivalType: ConfirmedArrivalType,
      stubbing: () -> Unit,
      verification: (Confirmation) -> InmateDetail,
    ) {
      whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
        PRISONER_DETAILS_PROTOTYPE.copy(isCurrentPrisoner = false),
      )
      stubbing()

      val confirmation = Confirmation.Unexpected(
        CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE.copy(
          movementReasonCode = movementReasonCode,
        ),
      )

      val response = confirmationService.confirmArrival(confirmation)

      assertThat(response.prisonNumber).isEqualTo(PRISON_NUMBER)

      verification(confirmation)

      verify(arrivalListener).arrived(
        refEq(
          ArrivalEvent(
            arrivalId = null,
            prisonNumber = PRISON_NUMBER,
            prisonId = PRISON_ID,
            bookingId = BOOKING_ID,
            arrivalType = expectedArrivalType,
          ),
        ),
      )
    }
  }

  companion object {
    private const val ARRIVAL_ID = "beae6404-de16-406f-844a-7e043960d9ec"
    private const val PRISON_NUMBER = "A1111AA"
    private const val CRO_NUMBER = "12/4321"
    private const val PNC_NUMBER = "01/123456"
    private const val BOOKING_ID = 1L
    private const val FIRST_NAME = "Eric"
    private const val LAST_NAME = "Bloodaxe"
    private val DATE_OF_BIRTH: LocalDate = LocalDate.of(1961, 4, 1)
    private const val SEX = "M"
    private const val PRISON_ID = "NMI"
    private const val LOCATION_ID = 1

    private val LOCATION = "RECP"
    private val INMATE_DETAIL = InmateDetail(
      offenderNo = PRISON_NUMBER,
      bookingId = BOOKING_ID,
      assignedLivingUnit = AssignedLivingUnit(
        PRISON_ID,
        LOCATION_ID,
        LOCATION,
        "Nottingham (HMP)",
      ),
    )
    private val INMATE_DETAIL_NO_UNIT = InmateDetail(
      offenderNo = PRISON_NUMBER,
      bookingId = BOOKING_ID,
    )
    private val CONFIRM_COURT_RETURN_RESPONSE =
      ConfirmCourtReturnResponse(prisonNumber = PRISON_NUMBER, location = LOCATION, bookingId = BOOKING_ID)

    val CONFIRMED_ARRIVAL_DETAIL_PROTOTYPE = ConfirmArrivalDetail(
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      sex = SEX,
      prisonId = PRISON_ID,
      prisonNumber = PRISON_NUMBER,
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03",
      fromLocationId = "",
      commentText = "",
    )

    val CONFIRMED_COURT_RETURN_REQUEST_PROTOTYPE = ConfirmCourtReturnRequest(
      prisonId = PRISON_ID,
      prisonNumber = PRISON_NUMBER,
    )

    val PRISONER_DETAILS_PROTOTYPE = PrisonerDetails(
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DATE_OF_BIRTH,
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      croNumber = CRO_NUMBER,
      isCurrentPrisoner = false,
      sex = SEX,
      arrivalType = ArrivalType.NEW_BOOKING,
      arrivalTypeDescription = "",
    )

    @JvmStatic
    fun recallMovementReasonCodes(): Stream<String> = RECALL_MOVEMENT_REASON_CODES.stream()

    @JvmStatic
    fun nonRecallMovementReasonCodes(): Stream<String> =
      (ALL_MOVEMENT_REASON_CODES - RECALL_MOVEMENT_REASON_CODES).stream()
  }
}
