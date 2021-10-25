package uk.gov.justice.digital.hmpps.welcometoprison.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI()
    .servers(
      listOf(
        Server().url("https://welcome-api.prison.service.justice.gov.uk/").description("Prod"),
        Server().url("https://welcome-api-preprod.prison.service.justice.gov.uk/").description("PreProd"),
        Server().url("https://welcome-api-dev.prison.service.justice.gov.uk/").description("Development"),
        Server().url("http://localhost:8080").description("Local"),
      )
    )
    .info(
      Info().title("Welcome to prison API")
        .version(version)
        .description("Providing information about people entering prison")
        .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk"))
    )
}
