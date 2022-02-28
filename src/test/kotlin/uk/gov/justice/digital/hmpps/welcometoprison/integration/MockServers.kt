package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber

private const val MAPPINGS_DIRECTORY = "src/testIntegration/resources"

open class MockServer(port: Int) : WireMockServer(
  WireMockConfiguration.wireMockConfig()
    .port(port)
    .usingFilesUnderDirectory(MAPPINGS_DIRECTORY)
)

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
              """[{
                        "firstName": "JIM",
                        "lastName": "SMITH", 
                        "dateOfBirth": "1991-07-31",
                        "prisonerNumber": "A1278AA",
                        "pncNumber": "1234/1234589A",
                        "status": "INACTIVE OUT"
                       }]"""
            )
        )
    )
  }

  fun stubGetPrisoner(status: Int) {
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
              """[{
                        "firstName": "JIM",
                        "lastName": "SMITH", 
                        "dateOfBirth": "1991-07-31",
                        "prisonerNumber": "A1278AA",
                        "pncNumber": "1234/1234589A",
                        "croNumber": "1234/111111"
                       }]"""
            )
        )
    )
  }

  fun stubMatchByPrisonerNumbers(status: Int, prisonerAndPncNumbers: List<PrisonerAndPncNumber>) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/prisoner-numbers"
        )
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody(
              mapper.writeValueAsString(
                prisonerAndPncNumbers
              )
            )
        )
    )
  }
}
