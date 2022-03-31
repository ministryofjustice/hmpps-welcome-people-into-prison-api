package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
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

@Service
@Transactional
class ConfirmationService(
  private val prisonService: PrisonService,
  private val prisonerSearchService: PrisonerSearchService,
  private val confirmedArrivalRepository: ConfirmedArrivalRepository,
  private val locationFormatter: LocationFormatter,
  private val clock: Clock
) {
  fun confirmArrival(moveId: String, confirmation: ConfirmArrivalDetail): ConfirmArrivalResponse {

    if (confirmation.isNewToPrison) {
      return createAndAdmitOffender(confirmation, moveId)
    }

    val prisoner = prisonerSearchService.getPrisoner(confirmation.prisonNumber!!)

    with(prisoner) {
      return if (isCurrentPrisoner) throw IllegalArgumentException("Confirming arrival of active prisoner: '$prisonNumber' is not supported.")
      else admitOffender(confirmation, moveId, prisonNumber)
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
    confirmation: ConfirmCourtReturnRequest,
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
      )
    )
    return response
  }

  private fun admitOffender(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String,
    prisonNumber: String
  ): ConfirmArrivalResponse = when (confirmArrivalDetail.movementReasonCode) {
    in RECALL_MOVEMENT_REASON_CODES -> recallOffender(confirmArrivalDetail, moveId, prisonNumber)
    else -> admitOffenderOnNewBooking(confirmArrivalDetail, moveId, prisonNumber)
  }

  private fun admitOffenderOnNewBooking(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String,
    prisonNumber: String
  ): ConfirmArrivalResponse {
    val inmateDetail = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmArrivalDetail)

    confirmedArrivalRepository.save(
      ConfirmedArrival(
        movementId = moveId,
        prisonNumber = prisonNumber,
        timestamp = LocalDateTime.now(clock),
        arrivalType = ArrivalType.NEW_BOOKING_EXISTING_OFFENDER,
        prisonId = confirmArrivalDetail.prisonId!!,
        bookingId = inmateDetail.bookingId,
        arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      )
    )
    return createResponse(prisonNumber, inmateDetail)
  }

  private fun recallOffender(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String,
    prisonNumber: String
  ): ConfirmArrivalResponse {
    val inmateDetail = prisonService.recallOffender(prisonNumber, confirmArrivalDetail)

    confirmedArrivalRepository.save(
      ConfirmedArrival(
        movementId = moveId,
        prisonNumber = prisonNumber,
        timestamp = LocalDateTime.now(clock),
        arrivalType = ArrivalType.RECALL,
        prisonId = confirmArrivalDetail.prisonId!!,
        bookingId = inmateDetail.bookingId,
        arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      )
    )
    return createResponse(prisonNumber, inmateDetail)
  }

  private fun createAndAdmitOffender(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String
  ): ConfirmArrivalResponse {
    val prisonNumber = prisonService.createOffender(confirmArrivalDetail)
    val inmateDetail = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmArrivalDetail)
    confirmedArrivalRepository.save(
      ConfirmedArrival(
        movementId = moveId,
        prisonNumber = prisonNumber,
        timestamp = LocalDateTime.now(clock),
        arrivalType = ArrivalType.NEW_TO_PRISON,
        prisonId = confirmArrivalDetail.prisonId!!,
        bookingId = inmateDetail.bookingId,
        arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      )
    )
    return createResponse(prisonNumber, inmateDetail)
  }

  private fun createResponse(
    prisonNumber: String,
    inmateDetail: InmateDetail,
  ): ConfirmArrivalResponse {
    val location = inmateDetail.assignedLivingUnit?.description ?: throw IllegalArgumentException("Prisoner: '$prisonNumber' do not have assigned living unit")
    return ConfirmArrivalResponse(
      prisonNumber = prisonNumber,
      location = locationFormatter.format(location)
    )
  }
}
