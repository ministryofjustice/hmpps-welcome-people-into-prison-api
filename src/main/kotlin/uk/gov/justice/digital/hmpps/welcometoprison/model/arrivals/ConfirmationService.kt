package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.config.SecurityUserContext
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.ConflictException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalRepository
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

sealed class Confirmation(open val detail: ConfirmArrivalDetail) {
  val prisonNumber: String? get() = detail.prisonNumber
  val isNewToPrison: Boolean get() = detail.isNewToPrison
  val movementReasonCode: String get() = detail.movementReasonCode!!

  data class Expected(val arrivalId: String, override val detail: ConfirmArrivalDetail) : Confirmation(detail)
  data class Unexpected(override val detail: ConfirmArrivalDetail) : Confirmation(detail)
}

@Service
@Transactional
class ConfirmationService(
  private val prisonService: PrisonService,
  private val prisonerSearchService: PrisonerSearchService,
  private val confirmedArrivalRepository: ConfirmedArrivalRepository,
  private val locationFormatter: LocationFormatter,
  private val clock: Clock,
  private val securityUserContext: SecurityUserContext
) {
  fun confirmArrival(confirmation: Confirmation): ConfirmArrivalResponse {
    if (confirmation.isNewToPrison) {
      return createAndAdmitOffender(confirmation)
    }

    val prisoner = prisonerSearchService.getPrisoner(confirmation.prisonNumber!!)

    with(prisoner) {
      return if (isCurrentPrisoner) throw ConflictException("Confirming arrival of active prisoner: '$prisonNumber' is not supported.")
      else admitOffender(confirmation, prisonNumber)
    }
  }

  fun confirmReturnFromCourt(moveId: String, confirmation: ConfirmCourtReturnRequest): ConfirmCourtReturnResponse {

    val prisoner = prisonerSearchService.getPrisoner(confirmation.prisonNumber)

    return if (prisoner.isCurrentPrisoner)
      returnFromCourt(moveId, prisoner.prisonNumber, confirmation)
    else
      throw IllegalArgumentException("Confirming court return of inactive prisoner: '${prisoner.prisonNumber}' is not supported.")
  }

  private fun returnFromCourt(
    moveId: String,
    prisonNumber: String,
    confirmation: ConfirmCourtReturnRequest
  ): ConfirmCourtReturnResponse {
    val response = prisonService.returnFromCourt(confirmation.prisonId, prisonNumber)

    confirmedArrivalRepository.save(
      ConfirmedArrival(
        movementId = moveId,
        prisonNumber = prisonNumber,
        timestamp = LocalDateTime.now(clock),
        arrivalType = ArrivalType.COURT_TRANSFER,
        prisonId = confirmation.prisonId,
        bookingId = response.bookingId,
        arrivalDate = LocalDate.now(clock),
        username = securityUserContext.principal,
      )
    )
    return response
  }

  private fun admitOffender(confirmation: Confirmation, prisonNumber: String): ConfirmArrivalResponse =
    when (confirmation.movementReasonCode) {
      in RECALL_MOVEMENT_REASON_CODES -> recallOffender(confirmation, prisonNumber)
      else -> admitOffenderOnNewBooking(confirmation, prisonNumber)
    }

  private fun admitOffenderOnNewBooking(confirmation: Confirmation, prisonNumber: String): ConfirmArrivalResponse {
    val response = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmation.detail)

    whenIsExpectedArrival(confirmation) { arrivalId, detail ->
      confirmedArrivalRepository.save(
        ConfirmedArrival(
          movementId = arrivalId,
          prisonNumber = prisonNumber,
          timestamp = LocalDateTime.now(clock),
          arrivalType = ArrivalType.NEW_BOOKING_EXISTING_OFFENDER,
          prisonId = detail.prisonId!!,
          bookingId = response.bookingId,
          arrivalDate = LocalDate.now(clock),
          username = securityUserContext.principal,
        )
      )
    }
    return createResponse(prisonNumber, response)
  }

  private fun recallOffender(
    confirmation: Confirmation,
    prisonNumber: String
  ): ConfirmArrivalResponse {
    val response = prisonService.recallOffender(prisonNumber, confirmation.detail)

    whenIsExpectedArrival(confirmation) { arrivalId, detail ->
      confirmedArrivalRepository.save(
        ConfirmedArrival(
          movementId = arrivalId,
          prisonNumber = prisonNumber,
          timestamp = LocalDateTime.now(clock),
          arrivalType = ArrivalType.RECALL,
          prisonId = detail.prisonId!!,
          bookingId = response.bookingId,
          arrivalDate = LocalDate.now(clock),
          username = securityUserContext.principal,
        )
      )
    }
    return createResponse(prisonNumber, response)
  }

  private fun createAndAdmitOffender(confirmation: Confirmation): ConfirmArrivalResponse {
    val prisonNumber = prisonService.createOffender(confirmation.detail)
    val response = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmation.detail)

    whenIsExpectedArrival(confirmation) { arrivalId, detail ->
      confirmedArrivalRepository.save(
        ConfirmedArrival(
          movementId = arrivalId,
          prisonNumber = prisonNumber,
          timestamp = LocalDateTime.now(clock),
          arrivalType = ArrivalType.NEW_TO_PRISON,
          prisonId = detail.prisonId!!,
          bookingId = response.bookingId,
          arrivalDate = LocalDate.now(clock),
          username = securityUserContext.principal,
        )
      )
    }
    return createResponse(prisonNumber, response)
  }

  private fun createResponse(prisonNumber: String, inmateDetail: InmateDetail): ConfirmArrivalResponse {
    val location = inmateDetail.assignedLivingUnit?.description
      ?: throw IllegalArgumentException("Prisoner: '$prisonNumber' do not have assigned living unit")
    return ConfirmArrivalResponse(
      prisonNumber = prisonNumber,
      location = locationFormatter.format(location)
    )
  }

  private fun whenIsExpectedArrival(
    confirmation: Confirmation,
    op: (arrivalId: String, detail: ConfirmArrivalDetail) -> Unit
  ) {
    if (confirmation is Confirmation.Expected) {
      op(confirmation.arrivalId, confirmation.detail)
    }
  }
}
