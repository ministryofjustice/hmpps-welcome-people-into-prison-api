package uk.gov.justice.digital.hmpps.config.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.hmpps.kotlin.health.HealthPingCheck

@Component("prisonApi")
class PrisonApiHealth(@Qualifier("prisonApiHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("prisonRegisterApi")
class PrisonRegisterApiHealth(@Qualifier("prisonRegisterWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("prisonerSearchApi")
class PrisonerSearchApiHealth(@Qualifier("prisonerSearchApiHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

@Component("basmApi")
class BasmApiHealth(@Qualifier("basmApiHealthWebClient") webClient: WebClient) : HealthCheck(webClient, "/ping")

@Component("manageUsersApi")
class ManageUsersApiHealth(@Qualifier("manageUsersHealthWebClient") webClient: WebClient) : HealthPingCheck(webClient)

// Custom health check for BASM as it doesn't use /health/ping
abstract class HealthCheck(
  private val webClient: WebClient,
  private val path: String,
) : HealthIndicator {
  override fun health(): Health = webClient.get()
    .uri(path)
    .retrieve()
    .toBodilessEntity()
    .flatMap { Mono.just(Health.up().withDetail("HttpStatus", it.statusCode).build()) }
    .onErrorResume(WebClientResponseException::class.java) {
      Mono.just(Health.down(it).withDetail("body", it.responseBodyAsString).withDetail("HttpStatus", it.statusCode).build())
    }
    .onErrorResume(Exception::class.java) { Mono.just(Health.down(it).build()) }
    .block() ?: Health.unknown().build()
}
