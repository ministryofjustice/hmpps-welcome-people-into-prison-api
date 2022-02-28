package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchByPrisonerNumberRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.Prisoner
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber
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

  fun getPrisoner(matchPrisonerRequest: MatchPrisonerRequest): List<Prisoner> {
    return webClient.post()
      .uri("/prisoner-search/match-prisoners")
      .bodyValue(matchPrisonerRequest)
      .retrieve()
      .bodyToMono(typeReference<List<Prisoner>>())
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
