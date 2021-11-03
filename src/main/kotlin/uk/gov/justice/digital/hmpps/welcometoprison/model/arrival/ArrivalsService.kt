package uk.gov.justice.digital.hmpps.welcometoprison.model.arrival

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.Clock
import java.time.LocalDate

@Service
@Transactional
class ArrivalsService(
  private val basmService: BasmService,
  private val prisonService: PrisonService,
  private val prisonerSearchService: PrisonerSearchService,
  private val confirmedArrivalService: ConfirmedArrivalService,
  private val clock: Clock
) {
  fun getMovement(moveId: String): Arrival = augmentWithPrisonData(basmService.getArrival(moveId))

  fun getMovements(agencyId: String, date: LocalDate): List<Arrival> {
    val arrivals =
      basmService
        .getArrivals(agencyId, date, date)
        .map { augmentWithPrisonData(it) } +
        prisonService
          .getTransfers(agencyId, date)

    return confirmedArrivalService.extractConfirmedArrivalFromArrivals(agencyId, date, arrivals)
  }

  private fun augmentWithPrisonData(arrival: Arrival): Arrival {
    val candidates = prisonerSearchService.getCandidateMatches(arrival)

    val match = candidates.firstOrNull { arrival.isMatch(it) }

    return if (match != null)
      arrival.copy(
        pncNumber = match.pncNumber,
        prisonNumber = match.prisonerNumber,
        isCurrentPrisoner = match.isCurrentPrisoner
      )

    // Ignore any provided prison number, but assume PNC is fine
    else
      arrival.copy(prisonNumber = null)
  }

  @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
  fun confirmArrival(
    moveId: String,
    confirmArrivalDetail: ConfirmArrivalDetail
  ): ConfirmArrivalResponse {

    val arrival = getMovement(moveId)

    if (arrival.isCurrentPrisoner) throw IllegalArgumentException("The arrival is known to NOMIS and has a current booking. This scenario is not supported.")

    return when (arrival.prisonNumber) {
      null -> createAndAdmitOffender(confirmArrivalDetail, moveId)
      else -> admitOffender(confirmArrivalDetail, moveId, arrival.prisonNumber)
    }
  }

  private fun admitOffender(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String,
    prisonNumber: String
  ): ConfirmArrivalResponse = when (confirmArrivalDetail.movementReasonCode) {
    "xyz" -> recallOffender(confirmArrivalDetail, moveId, prisonNumber)
    else -> admitOffenderOnNewBooking(confirmArrivalDetail, moveId, prisonNumber)
  }

  private fun admitOffenderOnNewBooking(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String,
    prisonNumber: String
  ): ConfirmArrivalResponse {
    val bookingId = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmArrivalDetail)
    confirmedArrivalService.add(
      movementId = moveId,
      prisonNumber = prisonNumber,
      prisonId = confirmArrivalDetail.prisonId!!,
      bookingId = bookingId,
      arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      arrivalType = ArrivalType.NEW_BOOKING_EXISTING_OFFENDER
    )
    return ConfirmArrivalResponse(offenderNo = prisonNumber)
  }

  private fun recallOffender(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String,
    prisonNumber: String
  ): ConfirmArrivalResponse {
    val bookingId = prisonService.recallOffender(prisonNumber, confirmArrivalDetail)
    confirmedArrivalService.add(
      movementId = moveId,
      prisonNumber = prisonNumber,
      prisonId = confirmArrivalDetail.prisonId!!,
      bookingId = bookingId,
      arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      arrivalType = ArrivalType.RECALL
    )
    return ConfirmArrivalResponse(offenderNo = prisonNumber)
  }

  private fun createAndAdmitOffender(
    confirmArrivalDetail: ConfirmArrivalDetail,
    moveId: String
  ): ConfirmArrivalResponse {
    val prisonNumber = prisonService.createOffender(confirmArrivalDetail)
    val bookingId = prisonService.admitOffenderOnNewBooking(prisonNumber, confirmArrivalDetail)
    confirmedArrivalService.add(
      movementId = moveId,
      prisonNumber = prisonNumber,
      prisonId = confirmArrivalDetail.prisonId!!,
      bookingId = bookingId,
      arrivalDate = confirmArrivalDetail.bookingInTime?.toLocalDate() ?: LocalDate.now(clock),
      arrivalType = ArrivalType.NEW_TO_PRISON
    )
    return ConfirmArrivalResponse(offenderNo = prisonNumber)
  }

  companion object {
    fun Arrival.isMatch(result: MatchPrisonerResponse) = when {
      prisonNumber != null && pncNumber != null ->
        prisonNumber == result.prisonerNumber && pncNumber == result.pncNumber

      prisonNumber != null && pncNumber == null ->
        prisonNumber == result.prisonerNumber

      prisonNumber == null && pncNumber != null ->
        pncNumber == result.pncNumber

      else -> false
    }
  }

}
