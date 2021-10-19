package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.springframework.http.MediaType
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

  fun stubCreateOffender(offenderNo: String) {
    stubFor(
      post("/api/offenders")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
                {
                  "offenderNo": "$offenderNo",
                  "offenderId": 2582523,
                  "rootOffenderId": 2582523,
                  "firstName": "D",
                  "lastName": "RAP",
                  "dateOfBirth": "1961-01-04",
                  "age": 60,
                  "activeFlag": false,
                  "physicalAttributes": {
                    "sexCode": "F",
                    "gender": "Female"
                  },
                  "identifiers": []
                }
              """.trimIndent()
            )
        )
    )
  }

  fun stubCreateOffenderFails(status: Int) {
    stubFor(
      post("/api/offenders")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(status)
            .withBody("{}")
        )
    )
  }

  fun stubAdmitOnNewBooking(offenderNo: String) {
    stubFor(
      post("/api/offenders/$offenderNo/booking")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
              {
                "offenderNo": "$offenderNo",
                "bookingId": 1201493,
                "bookingNo": "38639A",
                "offenderId": 2582523,
                "rootOffenderId": 2582523,
                "firstName": "D",
                "lastName": "RAP",
                "dateOfBirth": "1961-01-04",
                "age": 60,
                "activeFlag": true,
                "agencyId": "NMI",
                "assignedLivingUnitId": 4019,
                "alertsCodes": [],
                "activeAlertCount": 0,
                "inactiveAlertCount": 0,
                "alerts": [],
                "assignedLivingUnit": {
                  "agencyId": "NMI",
                  "locationId": 4019,
                  "description": "RECP",
                  "agencyName": "Nottingham (HMP)"
                },
                "physicalAttributes": {
                  "sexCode": "F",
                  "gender": "Female"
                },
                "profileInformation": [],
                "inOutStatus": "IN",
                "identifiers": [],
                "sentenceDetail": {
                  "bookingId": 1201493
                },
                "sentenceTerms": [],
                "status": "ACTIVE IN",
                "statusReason": "ADM-C",
                "lastMovementTypeCode": "ADM",
                "lastMovementReasonCode": "C",
                "privilegeSummary": {
                  "bookingId": 1201493,
                  "iepLevel": "Entry",
                  "iepDate": "2021-10-14",
                  "iepTime": "2021-10-14T15:44:44.523286308",
                  "daysSinceReview": 0,
                  "iepDetails": []
                }
              }
              """.trimIndent()
            )
        )
    )
  }

  fun stubAdmitOnNewBookingFails(offenderNo: String, status: Int) {
    stubFor(
      post("/api/offenders/$offenderNo/booking")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(status)
            .withBody(
              """
              {
                "status": $status,
                "userMessage": "No prisoner found for prisoner number A0001AA",
                "developerMessage": "No prisoner found for prisoner number A0001AA"
              }
            """
            )
        )
    )
  }
}