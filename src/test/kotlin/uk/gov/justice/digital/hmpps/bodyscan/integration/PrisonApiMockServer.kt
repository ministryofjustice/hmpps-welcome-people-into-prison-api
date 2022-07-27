package uk.gov.justice.digital.hmpps.bodyscan.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import org.springframework.http.MediaType

class PrisonApiMockServer : WireMockServer(9005) {
  fun stubCreateOffenderFailsPrisonerAlreadyExist() {
    stubFor(
      post("/api/offenders")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(400)
            .withBody(
              """{
    "status": 400,
    "errorCode" : 30002,
    "userMessage": "Prisoner with PNC 09/222376Z already exists with ID G9934UO",
    "developerMessage": "Prisoner with PNC 09/222376Z already exists with ID G9934UO"
}"""
            )
        )
    )
  }
}
