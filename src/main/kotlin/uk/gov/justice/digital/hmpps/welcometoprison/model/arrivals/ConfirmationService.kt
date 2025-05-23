package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.config.ConflictException
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType.COURT_TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType.NEW_BOOKING_EXISTING_OFFENDER
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType.NEW_TO_PRISON
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType.RECALL
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService

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
  private val arrivalListener: ArrivalListener,
  private val locationFormatter: LocationFormatter,
) {
  fun confirmArrival(confirmation: Confirmation): ConfirmArrivalResponse {
    if (confirmation.isNewToPrison) {
      return createAndAdmitOffender(confirmation)
    }

    val prisoner = prisonerSearchService.getPrisoner(confirmation.prisonNumber!!)

    return when {
      prisoner.isCurrentPrisoner -> throw ConflictException("Confirming arrival of active prisoner: '$prisoner.prisonNumber' is not supported.")
      else -> admitOffender(confirmation, prisoner.prisonNumber)
    }
  }

  fun confirmReturnFromCourt(arrivalId: String, confirmation: ConfirmCourtReturnRequest): ConfirmCourtReturnResponse {
    val prisoner = prisonerSearchService.getPrisoner(confirmation.prisonNumber)

    return when {
      prisoner.isCurrentPrisoner -> returnFromCourt(arrivalId, prisoner.prisonNumber, confirmation)
      else -> throw ConflictException("Confirming court return of inactive prisoner: '${prisoner.prisonNumber}' is not supported.")
    }
  }

  private fun returnFromCourt(
    arrivalId: String,
    prisonNumber: String,
    confirmation: ConfirmCourtReturnRequest,
  ): ConfirmCourtReturnResponse {
    val response = prisonService.returnFromCourt(confirmation.prisonId, prisonNumber)

    arrivalListener.arrived(
      ArrivalEvent(
        arrivalId = arrivalId,
        prisonNumber = prisonNumber,
        arrivalType = COURT_TRANSFER,
        prisonId = confirmation.prisonId,
        bookingId = response.bookingId,
      ),
    )
    return response
  }

  private fun admitOffender(confirmation: Confirmation, prisonNumber: String): ConfirmArrivalResponse = when (confirmation.movementReasonCode) {
    in RECALL_MOVEMENT_REASON_CODES -> recallOffender(confirmation, prisonNumber)
    else -> admitOffenderOnNewBooking(confirmation, prisonNumber)
  }

  private fun admitOffenderOnNewBooking(confirmation: Confirmation, prisonNumber: String): ConfirmArrivalResponse {
    val result = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmation.detail)

    arrivalListener.arrived(confirmation.toEvent(result, NEW_BOOKING_EXISTING_OFFENDER))

    return result.toResponse()
  }

  private fun recallOffender(confirmation: Confirmation, prisonNumber: String): ConfirmArrivalResponse {
    val result = prisonService.recallOffender(prisonNumber, confirmation.detail)

    arrivalListener.arrived(confirmation.toEvent(result, RECALL))

    return result.toResponse()
  }

  private fun createAndAdmitOffender(confirmation: Confirmation): ConfirmArrivalResponse {
    val result = prisonService.createOffender(confirmation.detail)

    arrivalListener.arrived(confirmation.toEvent(result, NEW_TO_PRISON))

    return result.toResponse()
  }

  private fun Confirmation.toEvent(inmateDetail: InmateDetail, type: ConfirmedArrivalType) = ArrivalEvent(
    arrivalId = if (this is Confirmation.Expected) this.arrivalId else null,
    // This doesn't appear to be returned from NOMIS for new bookings so reading from data from frontend
    prisonId = detail.prisonId!!,
    prisonNumber = inmateDetail.offenderNo,
    bookingId = inmateDetail.bookingId,
    arrivalType = type,
  )

  private fun InmateDetail.toResponse() = ConfirmArrivalResponse(
    prisonNumber = this.offenderNo,
    location = locationFormatter.extract(this),
  )
}
