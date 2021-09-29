package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate
import java.util.Collections

@Service
class MovementService(
  val basmService: BasmService,
  val prisonService: PrisonService,
  val prisonerSearchService: PrisonerSearchService
) {
  fun getMovements(agencyId: String, date: LocalDate) =
    basmService.getMoves(agencyId, date, date) + prisonService.getMoves(agencyId, date)

  fun getMovementsMatchedWithPrisoner(agencyId: String, date: LocalDate): List<Movement> {
    val movements = getMovements(agencyId, date)
    return getMovementsMatchedWithPrisoner(movements)
  }

  private fun getMovementsMatchedWithPrisoner(movements: List<Movement>): List<Movement> {
    fun findPrisonerMatch(identifier: String?, identifierName: String): MatchPrisonerResponse? {
      val matches = identifier?.let { prisonerSearchService.matchPrisoner(it) } ?: emptyList()
      if (matches.size > 1) log.warn("Multiple matched Prison records for a Movement by $identifierName. There are ${matches.size} matched Prison records for $identifier")
      return matches.firstOrNull()
    }
    fun decorateMovementWithPrisonerMatch(movement: Movement): Movement {
      var prisonNumberToUse = movement.prisonNumber
      var pncToUse = movement.pncNumber

      val matchByPrisonNumber = findPrisonerMatch(movement.prisonNumber, "Prison Number")
      val matchByPncNumber = findPrisonerMatch(movement.pncNumber, "PNC Number")

      if (matchByPrisonNumber === null && matchByPncNumber === null) prisonNumberToUse = null
      if (matchByPrisonNumber === null && matchByPncNumber !== null) prisonNumberToUse = matchByPncNumber.prisonerNumber
      if (matchByPrisonNumber !== null && matchByPncNumber === null) pncToUse = matchByPrisonNumber.pncNumber
      if (matchByPrisonNumber !== null && matchByPncNumber !== null) {
        if (matchByPrisonNumber.prisonerNumber != matchByPncNumber.prisonerNumber || matchByPrisonNumber.pncNumber != matchByPncNumber.pncNumber) {
          prisonNumberToUse = null
          pncToUse = null
        }
      }

      return movement.copy(
        prisonNumber = prisonNumberToUse,
        pncNumber = pncToUse
      )
    }

    val movementsMatchedWithPrisoner: MutableList<Movement> = emptyList<Movement>().toMutableList()
    movements.forEach { movementsMatchedWithPrisoner += decorateMovementWithPrisonerMatch(it) }
    return Collections.unmodifiableList(movementsMatchedWithPrisoner)
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
