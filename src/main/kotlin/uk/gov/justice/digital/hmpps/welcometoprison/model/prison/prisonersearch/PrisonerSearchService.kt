package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.config.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.PotentialMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerDetails
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.PotentialMatchRequest

@Service
class PrisonerSearchService(@Autowired private val client: PrisonerSearchApiClient) {

  fun getPrisoner(prisonNumber: String): PrisonerDetails = client.getPrisoner(prisonNumber)?.toPrisonerDetails()
    ?: throw NotFoundException("Could not find prisoner with prisonNumber: '$prisonNumber'")

  fun getPncNumbers(prisonerNumbers: List<String>): Map<String, String?> = if (prisonerNumbers.isEmpty()) {
    emptyMap()
  } else {
    client.matchPncNumbersByPrisonerNumbers(prisonerNumbers).associate { it.prisonerNumber to it.pncNumber }
  }

  fun findPotentialMatches(request: MatchPrisonersRequest): List<PotentialMatch> {
    if (!request.isValid()) return emptyList()
    val results = client.matchPrisoner(request.toPotentialMatchRequest())
    log.info("Number of search results for potential matches: {}", results.size)
    return results.map { it.toPotentialMatch() }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private fun MatchPrisonersRequest.toPotentialMatchRequest() = PotentialMatchRequest(
      firstName = this.firstName,
      lastName = this.lastName,
      dateOfBirth = this.dateOfBirth,
      pncNumber = this.pncNumber,
      nomsNumber = this.prisonNumber,
    )
  }
}
