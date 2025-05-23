package uk.gov.justice.digital.hmpps.config.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

abstract class HealthCheck(private val webClient: WebClient, private val endpoint: String) : HealthIndicator {

  override fun health(): Health? = webClient.get()
    .uri(endpoint)
    .retrieve()
    .toEntity(String::class.java)
    .flatMap { Mono.just(Health.up().withDetail("HttpStatus", it?.statusCode).build()) }
    .onErrorResume(WebClientResponseException::class.java) {
      Mono.just(
        Health.down(it).withDetail("body", it.responseBodyAsString).withDetail("HttpStatus", it.statusCode).build(),
      )
    }
    .onErrorResume(Exception::class.java) { Mono.just(Health.down(it).build()) }
    .block()
}

@Component
class PrisonApiHealth(@Qualifier("prisonApiHealthWebClient") webClient: WebClient) : HealthCheck(webClient, "/health/ping")

@Component
class PrisonRegisterHealth(@Qualifier("prisonRegisterWebClient") webClient: WebClient) : HealthCheck(webClient, "/health/ping")

@Component
class PrisonerSearchApiHealth(@Qualifier("prisonerSearchApiHealthWebClient") webClient: WebClient) : HealthCheck(webClient, "/health/ping")

@Component
class BasmApiHealth(@Qualifier("basmApiHealthWebClient") webClient: WebClient) : HealthCheck(webClient, "/ping")

@Component
class ManageUsersApiHealth(@Qualifier("manageUsersHealthWebClient") webClient: WebClient) : HealthCheck(webClient, "/health/ping")
