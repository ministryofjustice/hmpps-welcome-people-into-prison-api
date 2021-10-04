package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.slf4j.LoggerFactory
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
  fun getMovements(agencyId: String, date: LocalDate) =
    getMovementsMatchedWithPrisoner(basmService.getMoves(agencyId, date, date)) + prisonService.getMoves(agencyId, date)

  private fun findPrisonerMatch(identifier: String?, identifierName: String): MatchPrisonerResponse? {
    val matches = identifier?.let { prisonerSearchService.matchPrisoner(it) } ?: emptyList()
    if (matches.size > 1) log.warn("Multiple matched Prison records for a Movement by $identifierName. There are ${matches.size} matched Prison records for $identifier")
    return matches.firstOrNull()
  }

  private fun decorateMovementWithPrisonerMatch(movement: Movement): Movement {
    val matchByPrisonNumber = findPrisonerMatch(movement.prisonNumber, "Prison Number")
    val matchByPncNumber = findPrisonerMatch(movement.pncNumber, "PNC Number")

    return movement.copy(
      prisonNumber = prisonNumberToUse(movement, matchByPrisonNumber, matchByPncNumber),
      pncNumber = pncToUse(movement, matchByPrisonNumber, matchByPncNumber)
    )
  }

  private fun getMovementsMatchedWithPrisoner(movements: List<Movement>): List<Movement> =
    movements.map(::decorateMovementWithPrisonerMatch)

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    fun prisonNumberToUse(
      movement: Movement,
      matchByPrisonNumber: MatchPrisonerResponse?,
      matchByPncNumber: MatchPrisonerResponse?
    ): String? = when {
      matchByPrisonNumber == null && matchByPncNumber == null -> null
      matchByPrisonNumber == null && matchByPncNumber != null -> matchByPncNumber.prisonerNumber
      matchByPrisonNumber != null && matchByPncNumber == null -> movement.prisonNumber
      matchByPrisonNumber != null && matchByPncNumber != null -> when {
        matchByPrisonNumber.prisonerNumber != matchByPncNumber.prisonerNumber -> null
        matchByPrisonNumber.pncNumber != matchByPncNumber.pncNumber -> null
        else -> movement.prisonNumber
      }
      else -> movement.prisonNumber
    }

    fun pncToUse(
      movement: Movement,
      matchByPrisonNumber: MatchPrisonerResponse?,
      matchByPncNumber: MatchPrisonerResponse?
    ): String? = when {
      matchByPrisonNumber != null && matchByPncNumber == null -> matchByPrisonNumber.pncNumber
      matchByPrisonNumber != null && matchByPncNumber != null -> when {
        matchByPrisonNumber.prisonerNumber != matchByPncNumber.prisonerNumber -> null
        matchByPrisonNumber.pncNumber != matchByPncNumber.pncNumber -> null
        else -> movement.pncNumber
      }
      else -> movement.pncNumber
    }
  }
}
