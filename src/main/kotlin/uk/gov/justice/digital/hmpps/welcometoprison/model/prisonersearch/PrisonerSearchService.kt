package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse

@Service
class PrisonerSearchService(@Autowired private val client: PrisonerSearchApiClient) {
  fun matchPrisoner(identifier: String): List<MatchPrisonerResponse> {
    return client.matchPrisoner(MatchPrisonerRequest(identifier))
  }
}
