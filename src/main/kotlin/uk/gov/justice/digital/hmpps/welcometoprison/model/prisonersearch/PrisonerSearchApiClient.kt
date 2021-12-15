package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.request.MatchByPrisonerNumberRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.PrisonerAndPncNumber
import uk.gov.justice.digital.hmpps.welcometoprison.model.typeReference

@Component
class PrisonerSearchApiClient(@Qualifier("prisonerSearchApiWebClient") private val webClient: WebClient) {

  fun matchPrisoner(matchPrisonerRequest: MatchPrisonerRequest): List<MatchPrisonerResponse> {
    return webClient.post()
      .uri("/prisoner-search/match-prisoners")
      .bodyValue(matchPrisonerRequest)
      .retrieve()
      .bodyToMono(typeReference<List<MatchPrisonerResponse>>())
      .block()
      ?: emptyList()
  }

  fun matchPncNumbersByPrisonerNumbers(matchByPrisonerNumberRequest: MatchByPrisonerNumberRequest): List<PrisonerAndPncNumber> {
    return webClient.post()
      .uri("/prisoner-search/prisoner-numbers")
      .bodyValue(matchByPrisonerNumberRequest)
      .retrieve()
      .bodyToMono(typeReference<List<PrisonerAndPncNumber>>())
      .block()
      ?: emptyList()
  }
}
