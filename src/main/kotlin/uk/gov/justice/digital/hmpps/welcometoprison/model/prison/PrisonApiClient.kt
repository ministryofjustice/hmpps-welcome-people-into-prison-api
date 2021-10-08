package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.typeReference

@Service
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {
  fun getPrisonerImage(offenderNumber: String): ByteArray? {
    return webClient.get()
      .uri("/api/bookings/offenderNo/$offenderNumber/image/data?fullSizeImage=false")
      .retrieve()
      .bodyToMono(ByteArray::class.java).block()
  }

  fun getPrisonTransfersEnRoute(agencyId: String): List<OffenderMovement> {
    return webClient.get()
      .uri("/api/movements/$agencyId/enroute")
      .retrieve()
      .bodyToMono(typeReference<List<OffenderMovement>>())
      .block() ?: emptyList()
  }
}
