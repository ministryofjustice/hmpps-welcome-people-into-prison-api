package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson

class PrisonApiMockServer : WireMockServer(9005) {

  fun stubGetPrisonTransfersEnRoute(prisonId: String) {
    stubFor(
      get("/api/movements/$prisonId/enroute")
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody("prisonTransfersEnRoute".loadJson(this))
        )
    )
  }
}
