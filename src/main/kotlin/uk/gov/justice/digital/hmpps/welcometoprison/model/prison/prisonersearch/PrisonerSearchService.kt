package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.PotentialMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchByPrisonerNumberRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber

@Service
class PrisonerSearchService(@Autowired private val client: PrisonerSearchApiClient) {

  fun getCandidateMatches(arrival: Arrival): List<MatchPrisonerResponse> {
    return getCandidateMatches(arrival.prisonNumber, arrival.pncNumber)
  }

  fun getCandidateMatches(prisonNumber: String?, pncNumber: String?): List<MatchPrisonerResponse> {
    val matchesByPrisonNumber = findMatches(prisonNumber, "Prison Number")
    val matchesByPncNumber = findMatches(pncNumber, "PNC Number")

    return matchesByPrisonNumber + matchesByPncNumber
  }

  private fun findMatches(identifier: String?, identifierName: String): List<MatchPrisonerResponse> {
    val matches = identifier?.let { this.matchPrisoner(it) } ?: emptyList()
    if (matches.size > 1) log.warn("Multiple matched Prison records for a Movement by $identifierName. There are ${matches.size} matched Prison records for $identifier")
    return matches
  }

  private fun matchPrisoner(identifier: String): List<MatchPrisonerResponse> =
    client.matchPrisoner(MatchPrisonerRequest(identifier))

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun getPncNumbers(prisonerNumbers: List<String>): Map<String, String?> {
    val pncNumbers = matchPncNumbersByPrisonerNumbers(prisonerNumbers)
    return pncNumbers.associate { it.prisonerNumber to it.pncNumber }
  }

  private fun matchPncNumbersByPrisonerNumbers(prisonerNumbers: List<String>): List<PrisonerAndPncNumber> =
    if (prisonerNumbers.isEmpty()) emptyList() else client.matchPncNumbersByPrisonerNumbers(
      MatchByPrisonerNumberRequest(
        prisonerNumbers
      )
    )

  fun findPotentialMatch(matchPrisonersRequest: MatchPrisonersRequest): List<PotentialMatch> {
    var list = mutableListOf<MatchPrisonerResponse>()
    var listPm = mutableListOf<PotentialMatch>()

    list.addAll(this.getCandidateMatches(matchPrisonersRequest.prisonNumber, matchPrisonersRequest.pncNumber))


    return listPm
  }
}
