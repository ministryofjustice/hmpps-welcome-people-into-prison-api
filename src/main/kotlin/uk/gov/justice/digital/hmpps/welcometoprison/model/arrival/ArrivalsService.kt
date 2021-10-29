package uk.gov.justice.digital.hmpps.welcometoprison.model.arrival

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.AdmitArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.AdmitArrivalResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

@Service
class ArrivalsService(
  val basmService: BasmService,
  val prisonService: PrisonService,
  val prisonerSearchService: PrisonerSearchService,
  val confirmedArrivalService: ConfirmedArrivalService
) {
  fun getMovement(moveId: String): Arrival = augmentWithPrisonData(basmService.getArrival(moveId))

  fun getMovements(agencyId: String, date: LocalDate): List<Arrival> {
    val arrivals =
      basmService.getArrivals(agencyId, date, date).map { augmentWithPrisonData(it) } + prisonService.getTransfers(
        agencyId,
        date
      )
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
  fun admitArrival(
    moveId: String,
    admitArrivalDetail: AdmitArrivalDetail
  ): AdmitArrivalResponse {

    val arrival = getMovement(moveId)
    if (arrival.isCurrentPrisoner) throw IllegalArgumentException("The arrival is known to NOMIS and has a current booking. This scenario is not supported.")

    val prisonNumber = arrival.prisonNumber ?: prisonService.createOffender(admitArrivalDetail)

    prisonService.admitOffenderOnNewBooking(prisonNumber, admitArrivalDetail)

    return AdmitArrivalResponse(offenderNo = prisonNumber)
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
