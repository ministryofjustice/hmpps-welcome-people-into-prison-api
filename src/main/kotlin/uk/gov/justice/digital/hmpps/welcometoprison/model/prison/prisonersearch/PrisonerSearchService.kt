package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.PotentialMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerDetails
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.Prisoner

@Service
class PrisonerSearchService(@Autowired private val client: PrisonerSearchApiClient) {

  fun getPrisoner(prisonNumber: String): PrisonerDetails = client.getPrisoner(prisonNumber)?.toPrisonerDetails()
    ?: throw NotFoundException("Could not find prisoner with prisonNumber: '$prisonNumber'")

  fun getPncNumbers(prisonerNumbers: List<String>): Map<String, String?> {
    return if (prisonerNumbers.isEmpty()) emptyMap()
    else client.matchPncNumbersByPrisonerNumbers(prisonerNumbers).associate { it.prisonerNumber to it.pncNumber }
  }

  fun findPotentialMatches(request: MatchPrisonersRequest): List<PotentialMatch> {
    val results =
      findMatches(request.prisonNumber, "Prison Number") + //
        findMatches(request.pncNumber, "PNC Number") + //
        findNameAndDobMatches(request)

    val result = results.distinctBy { it.prisonNumber }
    log.info("Number of search results for potential matches: {}", result.size)
    return result
  }

  private fun findMatches(identifier: String?, identifierName: String): List<PotentialMatch> {
    val matches = identifier?.let { client.matchPrisoner(MatchPrisonerRequest(it)) } ?: emptyList()
    if (matches.size > 1) log.warn("Multiple matched Prison records for a Movement by $identifierName. There are ${matches.size} matched Prison records for $identifier")
    return matches.map { it.toPotentialMatch() }
  }

  private fun findNameAndDobMatches(request: MatchPrisonersRequest): List<PotentialMatch> {
    return if (request.lastName == null || request.dateOfBirth == null) emptyList()
    else
      client.matchPrisonerByNameAndDateOfBirth(
        SearchByNameAndDateOfBirth(
          request.firstName, request.lastName, request.dateOfBirth
        )
      ).map {
        it.toPotentialMatch()
      }
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)

    private fun MatchPrisonerResponse.toPotentialMatch() = PotentialMatch(
      firstName = Name.properCase(this.firstName),
      lastName = Name.properCase(this.lastName),
      dateOfBirth = this.dateOfBirth,
      prisonNumber = this.prisonerNumber,
      pncNumber = this.pncNumber,
      croNumber = this.croNumber,
      isCurrentPrisoner = this.isCurrentPrisoner,
      sex = this.gender,
    )

    private fun MatchPrisonerResponse.toPrisonerDetails() = PrisonerDetails(
      firstName = Name.properCase(this.firstName),
      lastName = Name.properCase(this.lastName),
      dateOfBirth = this.dateOfBirth,
      prisonNumber = this.prisonerNumber,
      pncNumber = this.pncNumber,
      croNumber = this.croNumber,
      isCurrentPrisoner = this.isCurrentPrisoner,
      sex = this.gender
    )

    private fun Prisoner.toPotentialMatch() = PotentialMatch(
      firstName = Name.properCase(this.firstName),
      lastName = Name.properCase(this.lastName),
      dateOfBirth = this.dateOfBirth,
      pncNumber = this.pncNumber,
      prisonNumber = this.prisonerNumber,
      croNumber = this.croNumber,
      sex = this.gender,
      isCurrentPrisoner = this.isCurrentPrisoner,
    )
  }
}
