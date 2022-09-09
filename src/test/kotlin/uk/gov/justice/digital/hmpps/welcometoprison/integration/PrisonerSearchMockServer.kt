package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import uk.gov.justice.digital.hmpps.config.MockServer
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber

class PrisonerSearchMockServer : MockServer(8093) {
  private val mapper = ObjectMapper()

  fun stubMatchPrisoners(status: Int) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/possible-matches\\\\?.*"
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
                        "croNumber": "SF80/655108T",
                        "status": "INACTIVE OUT",
                        "gender": "Male",
                        "prisonId": "MDI",
                        "lastMovementTypeCode": "REL"
                       }]"""
            )
        )
    )
  }

  fun stubGetPrisoner(status: Int) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/possible-matches\\\\?.*"
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
                        "croNumber": "SF80/655108T",
                        "status": "INACTIVE_OUT",
                        "gender": "Male",
                        "prisonId": "MDI",
                        "lastMovementTypeCode": "REL"
                       }]"""
            )
        )
    )
  }

  fun stubGetPrisonerWithNoBooking(status: Int) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/possible-matches\\\\?.*"
        )
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(status)
            .withBody(
              """[{
                  "prisonerNumber": "A9205DY",
                  "firstName": "EDUARDO",
                  "lastName": "STARK",
                  "dateOfBirth": "1994-01-01",
                  "gender": "Male",
                  "youthOffender": false,
                  "restrictedPatient": false
                }]"""
            )
        )
    )
  }

  fun stubGetActivePrisoner(status: Int) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/possible-matches\\\\?.*"
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
                        "croNumber": "SF80/655108T",
                        "status": "ACTIVE IN",
                        "gender": "Male",
                        "prisonId": "MDI",
                        "lastMovementTypeCode": "ADM"
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
