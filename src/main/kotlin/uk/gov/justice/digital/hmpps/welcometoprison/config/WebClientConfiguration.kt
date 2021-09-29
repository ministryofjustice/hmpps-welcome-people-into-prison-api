package uk.gov.justice.digital.hmpps.welcometoprison.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration(
  @Value("\${oauth.endpoint.url}") private val oauthRootUri: String,
  @Value("\${prison.endpoint.url}") private val prisonApiBaseUrl: String,
  @Value("\${basm.endpoint.url}") private val basmRootUri: String,
  @Value("\${prisoner.search.endpoint.url}") private val prisonerSearchApiUrl: String
) {

  @Bean
  fun oauthApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("hmpps-auth-api")

    val exchangeStrategies = ExchangeStrategies.builder()
      .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
      .build()

    return WebClient.builder()
      .baseUrl(oauthRootUri)
      .apply(oauth2Client.oauth2Configuration())
      .exchangeStrategies(exchangeStrategies)
      .build()
  }

  @Bean
  fun oauthApiHealthWebClient(): WebClient {
    return WebClient.builder().baseUrl(oauthRootUri).build()
  }

  @Bean
  fun prisonApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("prison-api")

    val exchangeStrategies = ExchangeStrategies.builder()
      .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
      .build()

    return WebClient.builder()
      .baseUrl(prisonApiBaseUrl)
      .apply(oauth2Client.oauth2Configuration())
      .exchangeStrategies(exchangeStrategies)
      .build()
  }

  @Bean
  fun prisonApiHealthWebClient(): WebClient {
    return WebClient.builder().baseUrl(prisonApiBaseUrl).build()
  }

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
          .build()
      )
      .build()
  }

  @Bean
  fun prisonerSearchApiHealthWebClient(): WebClient {
    return WebClient.builder().baseUrl(prisonerSearchApiUrl).build()
  }

  @Bean
  fun prisonerSearchApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("prisoner-search-api")

    val exchangeStrategies = ExchangeStrategies.builder()
      .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
      .build()

    return WebClient.builder()
      .baseUrl(prisonerSearchApiUrl)
      .apply(oauth2Client.oauth2Configuration())
      .exchangeStrategies(exchangeStrategies)
      .build()
  }

  @Bean
  fun basmApiHealthWebClient(): WebClient {
    return WebClient.builder().baseUrl(basmRootUri).build()
  }

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository?,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService?
  ): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager =
      AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }
}
