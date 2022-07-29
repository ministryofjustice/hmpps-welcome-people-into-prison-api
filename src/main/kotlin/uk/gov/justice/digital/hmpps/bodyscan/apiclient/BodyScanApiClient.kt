package uk.gov.justice.digital.hmpps.bodyscan.apiclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.config.typeReference
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.PotentialMatchRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse

class BodyScanApiClient (@Qualifier("prisonerSearchApiWebClient") private val webClient: WebClient) {

  fun getBulk(potentialMatchRequest: PotentialMatchRequest): List<MatchPrisonerResponse> {
    return webClient.post()
      .uri("/prisoner-search/possible-matches")
      .bodyValue(potentialMatchRequest)
      .retrieve()
      .bodyToMono(typeReference<List<MatchPrisonerResponse>>())
      .block()
      ?: emptyList()
  }



}