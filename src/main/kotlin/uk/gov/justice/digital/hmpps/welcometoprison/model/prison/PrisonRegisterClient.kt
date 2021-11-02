package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.welcometoprison.model.typeReference

data class Prison(@JsonProperty("prisonName") val description: String)

@Component
class PrisonRegisterClient(
  @Qualifier("prisonRegisterWebClient") private val webClient: WebClient
) {
  fun getPrison(prisonId: String): Prison? =
    webClient
      .get()
      .uri("/prisons/id/$prisonId")
      .retrieve()
      .bodyToMono(typeReference<Prison>())
      .onErrorResume(WebClientResponseException::class.java) { emptyWhenNotFound(it) }
      .block()
}