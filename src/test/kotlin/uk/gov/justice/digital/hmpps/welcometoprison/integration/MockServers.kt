package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse

private const val MAPPINGS_DIRECTORY = "src/testIntegration/resources"

open class MockServer(port: Int) : WireMockServer(
  WireMockConfiguration.wireMockConfig()
    .port(port)
    .usingFilesUnderDirectory(MAPPINGS_DIRECTORY)
)

class PrisonMockServer : MockServer(8092)

class HmppsAuthMockServer : MockServer(8090) {
  private val mapper = ObjectMapper()

  fun stubGrantToken() {
    stubFor(
      WireMock.post(WireMock.urlEqualTo("/auth/oauth/token"))
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody(mapper.writeValueAsString(mapOf("access_token" to "ABCDE", "token_type" to "bearer")))
        )
    )
  }
}

class PrisonerSearchMockServer : MockServer(8093) {
  private val mapper = ObjectMapper()

  fun stubMatchPrisoners(status: Int) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/match-prisoners\\\\?.*"
        )
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody(
              mapper.writeValueAsString(
                listOf(
                  MatchPrisonerResponse(
                    prisonerNumber = "A1278AA",
                    pncNumber = "1234/1234589A",
                  )
                )
              )
            )
        )
    )
  }
}
