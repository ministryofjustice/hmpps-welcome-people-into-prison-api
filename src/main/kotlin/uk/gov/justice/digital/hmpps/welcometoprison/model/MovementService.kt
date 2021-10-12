package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

@Service
class MovementService(
  val basmService: BasmService,
  val prisonService: PrisonService,
  val prisonerSearchService: PrisonerSearchService
) {
  fun getMovement(moveId: String): Movement = addPrisonData(basmService.getMovement(moveId))

  fun getMovements(agencyId: String, date: LocalDate) =
    basmService.getMoves(agencyId, date, date).map { addPrisonData(it) } + prisonService.getMoves(agencyId, date)

  private fun addPrisonData(movement: Movement): Movement {
    val candidates = prisonerSearchService.getCandidateMatches(movement)

    val match = candidates.firstOrNull { movement.isMatch(it) }

    return if (match != null)
      movement.copy(pncNumber = match.pncNumber, prisonNumber = match.prisonerNumber)

    // Ignore any provided prison number, but assume PNC is fine
    else
      movement.copy(prisonNumber = null)
  }

  companion object {
    fun Movement.isMatch(move: MatchPrisonerResponse) = when {
      prisonNumber != null && pncNumber != null ->
        prisonNumber == move.prisonerNumber && pncNumber == move.pncNumber

      prisonNumber != null && pncNumber == null ->
        prisonNumber == move.prisonerNumber

      prisonNumber == null && pncNumber != null ->
        pncNumber == move.pncNumber

      else -> false
    }
  }
}
