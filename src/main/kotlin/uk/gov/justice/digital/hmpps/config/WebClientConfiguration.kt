package uk.gov.justice.digital.hmpps.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.SYSTEM_USERNAME
import java.time.Duration
import uk.gov.justice.hmpps.kotlin.auth.authorisedWebClient

@Configuration
class WebClientConfiguration(
  @Value("\${prison.endpoint.url}") private val prisonApiBaseUrl: String,
  @Value("\${basm.endpoint.url}") private val basmRootUri: String,
  @Value("\${prisoner.search.endpoint.url}") private val prisonerSearchApiUrl: String,
  @Value("\${prison.register.endpoint.url}") private val prisonRegisterApiUrl: String,
  @Value("\${manage.users.endpoint.url}") private val manageUsersApiUri: String,
  @Value("\${api.timeout:20s}") val healthTimeout: Duration,
) {

  @Bean
  fun manageUsersHealthWebClient(): WebClient = WebClient.builder().baseUrl(manageUsersApiUri).build()

  @Bean
  fun manageUsersWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient =
    WebClient.builder().authorisedWebClient(authorizedClientManager, registrationId = SYSTEM_USERNAME, url = manageUsersApiUri, healthTimeout)

  @Bean
  fun prisonApiWebClient(): WebClient {
    val exchangeStrategies = ExchangeStrategies.builder()
      .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
      .build()

    return WebClient.builder()
      .baseUrl(prisonApiBaseUrl)
      .filter(addAuthHeaderFilterFunction())
      .exchangeStrategies(exchangeStrategies)
      .build()
  }

  @Bean
  fun prisonApiHealthWebClient(): WebClient = WebClient.builder().baseUrl(prisonApiBaseUrl).build()

  @Bean
  fun basmApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("basm-api")

    return WebClient.builder()
      .baseUrl(basmRootUri)
      .apply(oauth2Client.oauth2Configuration())
      .exchangeStrategies(
        ExchangeStrategies.builder()
          .codecs { configurer ->
            configurer.defaultCodecs()
              .maxInMemorySize(-1)
          }
          .build(),
      )
      .build()
  }

  @Bean
  fun prisonerSearchApiHealthWebClient(): WebClient = WebClient.builder().baseUrl(prisonerSearchApiUrl).build()

  @Bean
  fun prisonerSearchApiWebClient(): WebClient {
    val exchangeStrategies = ExchangeStrategies.builder()
      .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
      .build()

    return WebClient.builder()
      .baseUrl(prisonerSearchApiUrl)
      .filter(addAuthHeaderFilterFunction())
      .exchangeStrategies(exchangeStrategies)
      .build()
  }

  @Bean
  fun basmApiHealthWebClient(): WebClient = WebClient.builder().baseUrl(basmRootUri).build()

  @Bean
  fun prisonRegisterWebClient(): WebClient = WebClient.builder().baseUrl(prisonRegisterApiUrl).build()

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository?,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService?,
  ): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager =
      AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  private fun addAuthHeaderFilterFunction() = ExchangeFilterFunction { request: ClientRequest, next: ExchangeFunction ->
    val token = when (val authentication = SecurityContextHolder.getContext().authentication) {
      is AuthAwareAuthenticationToken -> authentication.token.tokenValue
      else -> throw IllegalStateException("Auth token not present")
    }

    next.exchange(
      ClientRequest.from(request)
        .header(AUTHORIZATION, "Bearer $token")
        .build(),
    )
  }
}
