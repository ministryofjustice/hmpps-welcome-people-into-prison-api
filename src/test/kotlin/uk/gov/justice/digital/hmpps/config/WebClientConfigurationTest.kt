package uk.gov.justice.digital.hmpps.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.convert.ApplicationConversionService
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.digitalprisonreportinglib.security.UserPermissionProviderAutoConfig

class WebClientConfigurationTest {
  private val contextRunner = ApplicationContextRunner()
    .withInitializer { it.beanFactory.conversionService = ApplicationConversionService.getSharedInstance() }
    .withUserConfiguration(
      WebClientConfiguration::class.java,
      UserPermissionProviderAutoConfig::class.java,
      OAuthClientTestConfiguration::class.java,
    )
    .withPropertyValues(
      "prison.endpoint.url=http://localhost:9005",
      "basm.endpoint.url=http://localhost:9004",
      "prisoner.search.endpoint.url=http://localhost:8093",
      "prison.register.endpoint.url=http://localhost:9006",
      "manage.users.endpoint.url=http://localhost:9007",
      "dpr.lib.manage-users.host=http://localhost:9007",
    )

  @Test
  fun `application and reporting OAuth clients coexist without bean overriding`() {
    contextRunner
      .withPropertyValues("dpr.lib.system.role=DPR_API_ACCESS")
      .run { context ->
        assertThat(context).hasNotFailed()
        assertThat(context).hasBean("basmApiWebClient")
        assertThat(context).hasBean("manageUsersWebClient")
        assertThat(context).hasBean("userPermissionProvider")
        assertThat(context.getBeansOfType(OAuth2AuthorizedClientManager::class.java))
          .containsOnlyKeys("basmAuthorizedClientManager", "authorizedClientManager")
      }
  }

  @Test
  fun `BASM OAuth client does not depend on reporting being enabled`() {
    contextRunner.run { context ->
      assertThat(context).hasNotFailed()
      assertThat(context).hasBean("basmApiWebClient")
      assertThat(context).doesNotHaveBean("manageUsersWebClient")
      assertThat(context.getBeansOfType(OAuth2AuthorizedClientManager::class.java))
        .containsOnlyKeys("basmAuthorizedClientManager")
    }
  }

  @Configuration(proxyBeanMethods = false)
  class OAuthClientTestConfiguration {
    @Bean
    fun clientRegistrationRepository(): ClientRegistrationRepository = mock()

    @Bean
    fun authorizedClientService(): OAuth2AuthorizedClientService = mock()

    @Bean
    fun webClientBuilder(): WebClient.Builder = WebClient.builder()
  }
}
