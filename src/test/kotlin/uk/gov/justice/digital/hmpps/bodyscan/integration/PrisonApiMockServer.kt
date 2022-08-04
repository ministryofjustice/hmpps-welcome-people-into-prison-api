package uk.gov.justice.digital.hmpps.bodyscan.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import org.springframework.http.MediaType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PrisonApiMockServer : WireMockServer(9005) {
  fun stubGetPersonalCareNeedsForPrisonNumbers(type: String, fromStartDate: LocalDate, toStartDate: LocalDate, prisonNumbers: List<String>) {
    stubFor(
      post(
        "/api/bookings/offenderNo/personal-care-needs/count?type=" +
          "$type" +
          "&fromStartDate=${fromStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}" +
          "&toStartDate=${toStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
      ).withRequestBody(equalToJson(prisonNumbers.joinToString(prefix = "[", postfix = "]") { "\"$it\"" }))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """[
    {
        "offenderNo": "G8266VG",
        "size": 1
    },
    {
        "offenderNo": "G8874VT",
        "size": 24
    },
    {
        "offenderNo": "G8874VU",
        "size": 0
    },
        {
        "offenderNo": "G8874VV",
        "size": 99
    },
    {
        "offenderNo": "G8874VW",
        "size": 100
    },
        {
        "offenderNo": "G8874VX",
        "size": 115
    },
    {
        "offenderNo": "G8874VY",
        "size": 116
    },
    {
        "offenderNo": "G8874VZ",
        "size": 117
    }
]"""
            )
        )
    )
  }

  fun stubGetOffenderDetails(prisonNumber: String, statusCode: Int) {
    stubFor(
      get("/api/offenders/$prisonNumber")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(statusCode)
            .withBody(
              """{
    "offenderNo": "$prisonNumber",
    "bookingId": 1202691,
    "bookingNo": "39728A",
    "offenderId": 2581930,
    "rootOffenderId": 2581930,
    "firstName": "GEOFF",
    "lastName": "BROWN",
    "dateOfBirth": "1992-03-12",
    "age": 30,
    "activeFlag": true,
    "agencyId": "LII",
    "assignedLivingUnitId": 4007,
    "alertsCodes": [],
    "activeAlertCount": 0,
    "inactiveAlertCount": 0,
    "alerts": [],
    "assignedLivingUnit": {
        "agencyId": "LII",
        "locationId": 4007,
        "description": "RECP",
        "agencyName": "Lincoln (HMP)"
    },
    "physicalAttributes": {
        "sexCode": "M",
        "gender": "Male",
        "raceCode": "W1",
        "ethnicity": "White: Eng./Welsh/Scot./N.Irish/British"
    },
    "physicalCharacteristics": [],
    "profileInformation": [
        {
            "type": "YOUTH",
            "question": "Youth Offender?",
            "resultValue": "No"
        }
    ],
    "physicalMarks": [],
    "inOutStatus": "IN",
    "identifiers": [
        {
            "type": "PNC",
            "value": "2003/0011991D",
            "offenderNo": "A5202DY",
            "issuedDate": "2020-08-18",
            "caseloadType": "INST"
        }
    ],
    "personalCareNeeds": [],
    "sentenceDetail": {
        "bookingId": 1202691
    },
    "offenceHistory": [],
    "sentenceTerms": [],
    "aliases": [],
    "status": "ACTIVE IN",
    "statusReason": "ADM-N",
    "lastMovementTypeCode": "ADM",
    "lastMovementReasonCode": "N",
    "legalStatus": "REMAND",
    "recall": false,
    "imprisonmentStatus": "RECEP_REM",
    "imprisonmentStatusDescription": "On remand (reception)",
    "privilegeSummary": {
        "bookingId": 1202691,
        "iepLevel": "Entry",
        "iepDate": "2022-08-02",
        "iepTime": "2022-08-02T10:07:42",
        "daysSinceReview": 2,
        "iepDetails": []
    },
    "receptionDate": "2022-08-02",
    "locationDescription": "Lincoln (HMP)",
    "latestLocationId": "LII"
}"""
            )
        )
    )
  }

  fun stubAddPersonalCareNeeds(
    bookingId: Long,
    date: LocalDate
  ) {
    stubFor(
      post(
        "/api/bookings/$bookingId/personal-care-needs"
      ).withRequestBody(
        equalToJson(
          """
        {
          "problemCode": "BSC6.0",
          "problemStatus": "ON",
          "commentText": "Intelligence-Positive",
          "startDate": "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}",
          "endDate": "${date.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
        }
          """.trimIndent()
        )
      )
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(201)
        )
    )
  }
}
