package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse

@Service
class PrisonerSearchApiClient(@Qualifier("prisonerSearchApiWebClient") private val webClient: WebClient) {
  private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

  fun matchPrisoner(matchPrisonerRequest: MatchPrisonerRequest): List<MatchPrisonerResponse> {
    return webClient.post()
      .uri("/prisoner-search/match-prisoners")
      .bodyValue(matchPrisonerRequest)
      .retrieve()
      .bodyToMono(typeReference<List<MatchPrisonerResponse>>())
      .block()
      ?: emptyList()
  }
}
