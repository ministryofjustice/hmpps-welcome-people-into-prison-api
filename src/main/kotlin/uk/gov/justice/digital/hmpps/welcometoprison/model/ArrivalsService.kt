package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

@Service
class ArrivalsService(
  val basmService: BasmService,
  val prisonService: PrisonService,
  val prisonerSearchService: PrisonerSearchService
) {
  fun getMovement(moveId: String): Arrival = addPrisonData(basmService.getArrival(moveId))

  fun getMovements(agencyId: String, date: LocalDate): List<Arrival> {
    val arrivals = basmService.getArrivals(agencyId, date, date)
    return arrivals.map { addPrisonData(it) } +
      prisonService.getTransfers(agencyId, date)
  }

  private fun addPrisonData(arrival: Arrival): Arrival {
    val candidates = prisonerSearchService.getCandidateMatches(arrival)

    val match = candidates.firstOrNull { arrival.isMatch(it) }

    return if (match != null)
      arrival.copy(pncNumber = match.pncNumber, prisonNumber = match.prisonerNumber, isCurrentPrisoner = match.currentPrisoner)

    // Ignore any provided prison number, but assume PNC is fine
    else
      arrival.copy(prisonNumber = null)
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
