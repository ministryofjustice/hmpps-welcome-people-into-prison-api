package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {
  private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

  fun getPrisonerImages(offenderNumber: String): List<PrisonerImage> {
    return webClient.get()
      .uri("/api/images/offenders/$offenderNumber")
      .retrieve()
      .bodyToMono(typeReference<List<PrisonerImage>>())
      .block()
      ?: emptyList()
  }

  fun getPrisonerImage(imageId: Number): ByteArray? {
    return webClient.get()
      .uri("/api/images/$imageId/data?fullSizeImage=false")
      .retrieve()
      .bodyToMono(ByteArray::class.java).block()
  }
}
