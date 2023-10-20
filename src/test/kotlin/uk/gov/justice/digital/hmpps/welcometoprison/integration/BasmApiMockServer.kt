package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson

class BasmApiMockServer : WireMockServer(9004) {

  fun stubGrantToken(expires: Int = 10_000) {
    stubFor(
      post(urlEqualTo("/oauth/token"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withBody("""{ "access_token": "ABCDE", "token_type": "bearer", "expires_in": $expires }"""),
        ),
    )
  }

  fun stubGetPrison(status: Int) {
    stubFor(
      get(
        urlPathMatching(
          "/api/reference/locations\\\\?.*",
        ),
      )
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody("prison".loadJson(this)),
        ),
    )
  }

  fun stubGetMovements(status: Int, hasMissingDob: Boolean = false) {
    stubFor(
      get(
        urlPathMatching("/api/moves\\\\?.*"),
      )
        .withHeader("X-Current-User", WireMock.equalTo("welcome-into-prison-client"))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody(if (!hasMissingDob) "moves".loadJson(this) else "moves-with-missing-dob".loadJson(this)),
        ),
    )
  }
  fun stubGetMovementWithOffence(id: String, status: Int, body: String? = "move-with-offence".loadJson(this)) {
    stubGetMovement(id, status, body)
  }
  fun stubGetMovement(id: String, status: Int, body: String? = "move".loadJson(this)) {
    stubFor(
      get(
        urlPathMatching("/api/moves/$id"),
      )
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody(body),
        ),
    )
  }
}
