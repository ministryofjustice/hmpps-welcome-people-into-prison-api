package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders

class PrisonRegisterMockServer : WireMockServer(9006) {

  fun stubGetPrison(prisonId: String, status: Int = 200) {
    stubFor(
      WireMock.get("/prisons/id/$prisonId")
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody(
              """
              {
                "prisonId": "$prisonId",
                "prisonName": "Nottingham (HMP)",
                "active": true
              }
              """.trimIndent(),
            ),
        ),
    )
  }
}
