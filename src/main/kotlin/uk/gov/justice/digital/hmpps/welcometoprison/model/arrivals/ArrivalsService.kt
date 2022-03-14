package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest
import java.time.Clock
import java.time.LocalDate

@Service
@Transactional
class ArrivalsService(
  private val basmService: BasmService,
  private val prisonService: PrisonService,
  private val prisonerSearchService: PrisonerSearchService,
  private val confirmedArrivalService: ConfirmedArrivalService,
  private val locationFormatter: LocationFormatter,
  private val clock: Clock
) {
  fun getArrival(moveId: String): Arrival = augmentWithPrisonData(basmService.getArrival(moveId))

  fun getArrivals(agencyId: String, date: LocalDate): List<Arrival> {
    val arrivals =
      basmService
        .getArrivals(agencyId, date, date)
        .map { augmentWithPrisonData(it) }

    return confirmedArrivalService.extractConfirmedArrivalFromArrivals(agencyId, date, arrivals)
  }

  private fun augmentWithPrisonData(arrival: Arrival): Arrival {
    val matches = prisonerSearchService.findPotentialMatches(
      MatchPrisonersRequest(
        arrival.firstName,
        arrival.lastName,
        arrival.dateOfBirth,
        arrival.prisonNumber,
        arrival.pncNumber
      )
    )

    return arrival.copy(
      isCurrentPrisoner = matches.isNotEmpty() && matches.all { it.isCurrentPrisoner },
      potentialMatches = matches
    )
  }

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

    confirmedArrivalService.add(
      moveId,
      prisonNumber,
      confirmation.prisonId,
      response.bookingId,
      LocalDate.now(clock),
      ArrivalType.COURT_TRANSFER
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
    confirmedArrivalService.add(
      movementId = moveId,
      prisonNumber = prisonNumber,
      prisonId = confirmArrivalDetail.prisonId!!,
      bookingId = inmateDetail.bookingId,
      arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      arrivalType = ArrivalType.NEW_BOOKING_EXISTING_OFFENDER
    )
    val livingUnitName = inmateDetail.assignedLivingUnit?.description
      ?: throw IllegalArgumentException("Prisoner: '$prisonNumber' do not have assigned living unit")
    return ConfirmArrivalResponse(prisonNumber = prisonNumber, location = locationFormatter.format(livingUnitName))
  }

  private fun recallOffender(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String,
    prisonNumber: String
  ): ConfirmArrivalResponse {
    val inmateDetail = prisonService.recallOffender(prisonNumber, confirmArrivalDetail)
    confirmedArrivalService.add(
      movementId = moveId,
      prisonNumber = prisonNumber,
      prisonId = confirmArrivalDetail.prisonId!!,
      bookingId = inmateDetail.bookingId,
      arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      arrivalType = ArrivalType.RECALL
    )
    val livingUnitName = inmateDetail.assignedLivingUnit?.description
      ?: throw IllegalArgumentException("Prisoner: '$prisonNumber' do not have assigned living unit")
    return ConfirmArrivalResponse(prisonNumber = prisonNumber, location = locationFormatter.format(livingUnitName))
  }

  private fun createAndAdmitOffender(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String
  ): ConfirmArrivalResponse {
    val prisonNumber = prisonService.createOffender(confirmArrivalDetail)
    val inmateDetail = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmArrivalDetail)
    confirmedArrivalService.add(
      movementId = moveId,
      prisonNumber = prisonNumber,
      prisonId = confirmArrivalDetail.prisonId!!,
      bookingId = inmateDetail.bookingId,
      arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      arrivalType = ArrivalType.NEW_TO_PRISON
    )
    val livingUnitName = inmateDetail.assignedLivingUnit?.description
      ?: throw IllegalArgumentException("Prisoner: '$prisonNumber' do not have assigned living unit")
    return ConfirmArrivalResponse(prisonNumber = prisonNumber, location = locationFormatter.format(livingUnitName))
  }
}
