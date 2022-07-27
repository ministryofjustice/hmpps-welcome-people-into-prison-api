package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.config.typeReference
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchByPrisonerNumberRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.PotentialMatchRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber

@Component
class PrisonerSearchApiClient(@Qualifier("prisonerSearchApiWebClient") private val webClient: WebClient) {

  fun matchPrisoner(potentialMatchRequest: PotentialMatchRequest): List<MatchPrisonerResponse> {
    return webClient.post()
      .uri("/prisoner-search/possible-matches")
      .bodyValue(potentialMatchRequest)
      .retrieve()
      .bodyToMono(typeReference<List<MatchPrisonerResponse>>())
      .block()
      ?: emptyList()
  }

  fun getPrisoner(prisonNumber: String): MatchPrisonerResponse? =
    matchPrisoner(PotentialMatchRequest(nomsNumber = prisonNumber)).firstOrNull()

  fun matchPncNumbersByPrisonerNumbers(prisonerNumbers: List<String>): List<PrisonerAndPncNumber> {
    return webClient.post()
      .uri("/prisoner-search/prisoner-numbers")
      .bodyValue(MatchByPrisonerNumberRequest(prisonerNumbers))
      .retrieve()
      .bodyToMono(typeReference<List<PrisonerAndPncNumber>>())
      .block()
      ?: emptyList()
  }
}
