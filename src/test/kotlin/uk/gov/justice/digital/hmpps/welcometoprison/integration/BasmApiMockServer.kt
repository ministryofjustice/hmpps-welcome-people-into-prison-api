package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class BasmApiMockServer : WireMockServer(9004) {

  fun stubGrantToken() {
    stubFor(
      post(urlEqualTo("/oauth/token"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody("""{ "access_token": "ABCDE", "token_type": "bearer" }""")
        )
    )
  }

  fun stubGetPrison(status: Int) {
    stubFor(
      get(
        urlPathMatching(
          "/api/reference/locations\\\\?.*"
        )
      )
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody("prison".loadJson())
        )
    )
  }

  fun stubGetMovements(status: Int) {
    stubFor(
      get(
        urlPathMatching("/api/moves\\\\?.*")
      )
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody("moves".loadJson())
        )
    )
  }

  private fun String.loadJson(): String =
    BasmApiMockServer::class.java.getResource("$this.json")?.readText()
      ?: throw AssertionError("file $this.json not found")
}
