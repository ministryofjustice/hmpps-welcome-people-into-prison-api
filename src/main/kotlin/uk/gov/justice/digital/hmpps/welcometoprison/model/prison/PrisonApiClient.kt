package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {
  fun getPrisonerImage(offenderNumber: String): ByteArray? {
    return webClient.get()
      .uri("/api/bookings/offenderNo/$offenderNumber/image/data?fullSizeImage=false")
      .retrieve()
      .bodyToMono(ByteArray::class.java).block()
  }
}
