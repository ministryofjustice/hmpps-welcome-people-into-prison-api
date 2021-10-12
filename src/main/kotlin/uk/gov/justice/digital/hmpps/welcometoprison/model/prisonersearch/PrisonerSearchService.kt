package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse

@Service
class PrisonerSearchService(@Autowired private val client: PrisonerSearchApiClient) {

  fun getCandidateMatches(movement: Movement): List<MatchPrisonerResponse> {
    val matchesByPrisonNumber = findMatches(movement.prisonNumber, "Prison Number")
    val matchesByPncNumber = findMatches(movement.pncNumber, "PNC Number")

    return matchesByPrisonNumber + matchesByPncNumber
  }

  private fun findMatches(identifier: String?, identifierName: String): List<MatchPrisonerResponse> {
    val matches = identifier?.let { this.matchPrisoner(it) } ?: emptyList()
    if (matches.size > 1) log.warn("Multiple matched Prison records for a Movement by $identifierName. There are ${matches.size} matched Prison records for $identifier")
    return matches
  }

  private fun matchPrisoner(identifier: String) = client.matchPrisoner(MatchPrisonerRequest(identifier))

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
