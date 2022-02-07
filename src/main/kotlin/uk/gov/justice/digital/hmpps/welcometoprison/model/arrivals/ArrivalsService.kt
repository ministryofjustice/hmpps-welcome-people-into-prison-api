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
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
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
    val candidates = prisonerSearchService.getCandidateMatches(arrival)

    val matches = candidates.filter { arrival.isMatch(it) }.distinctBy { it.prisonerNumber }

    return arrival.copy(
      isCurrentPrisoner = matches.isNotEmpty() && matches.all { it.isCurrentPrisoner },
      potentialMatches = matches.map { it.toPotentialMatch() }
    )
  }

  fun confirmArrival(
    moveId: String,
    confirmArrivalDetail: ConfirmArrivalDetail
  ): ConfirmArrivalResponse {

    val arrival = getArrival(moveId)

    return if (!arrival.isCurrentPrisoner)
      when (arrival.prisonNumber) {
        null -> createAndAdmitOffender(confirmArrivalDetail, moveId)
        else -> admitOffender(confirmArrivalDetail, moveId, arrival.prisonNumber)
      } else
      throw IllegalArgumentException("Confirming arrival of active prisoner: '${arrival.prisonNumber}' is not supported.")
  }

  fun confirmReturnFromCourt(
    moveId: String,
    confirmCourtReturnRequest: ConfirmCourtReturnRequest
  ): ConfirmCourtReturnResponse {

    val arrival = getArrival(moveId)

    return if (arrival.isCurrentPrisoner)
      when (arrival.fromLocationType) {
        LocationType.COURT -> returnFromCourt(moveId, confirmCourtReturnRequest, arrival)
        else -> throw IllegalArgumentException("The arrival is known to NOMIS, has a current booking but is not from court. This scenario is not supported.")
      } else
      throw IllegalArgumentException("Confirming court return of inactive prisoner: '${arrival.prisonNumber}' is not supported.")
  }

  private fun returnFromCourt(
    moveId: String,
    confirmCourtReturnRequest: ConfirmCourtReturnRequest,
    arrival: Arrival
  ): ConfirmCourtReturnResponse {

    val confirmCourtReturnResponse = prisonService.returnFromCourt(confirmCourtReturnRequest, arrival)

    confirmedArrivalService.add(
      moveId,
      arrival.prisonNumber!!,
      confirmCourtReturnRequest.prisonId!!,
      confirmCourtReturnResponse.bookingId,
      LocalDate.now(clock),
      ArrivalType.COURT_TRANSFER
    )
    return confirmCourtReturnResponse
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
    return ConfirmArrivalResponse(prisonNumber = prisonNumber, location = locationFormatter.format(inmateDetail.assignedLivingUnit.description))
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
    return ConfirmArrivalResponse(prisonNumber = prisonNumber, location = locationFormatter.format(inmateDetail.assignedLivingUnit.description))
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
    return ConfirmArrivalResponse(prisonNumber = prisonNumber, location = locationFormatter.format(inmateDetail.assignedLivingUnit.description))
  }

  companion object {
    fun Arrival.isMatch(result: MatchPrisonerResponse) = when {
      prisonNumber != null && pncNumber != null ->
        result.prisonerNumber.equals(prisonNumber, ignoreCase = true) && result.pncNumber.equals(
          pncNumber,
          ignoreCase = true
        )

      prisonNumber != null && pncNumber == null ->
        result.prisonerNumber.equals(prisonNumber, ignoreCase = true)

      prisonNumber == null && pncNumber != null ->
        result.pncNumber.equals(pncNumber, ignoreCase = true)

      else -> false
    }

    fun MatchPrisonerResponse.toPotentialMatch() = PotentialMatch(
      this.firstName,
      this.lastName,
      this.dateOfBirth,
      this.prisonerNumber,
      this.pncNumber,
    )
  }
}
