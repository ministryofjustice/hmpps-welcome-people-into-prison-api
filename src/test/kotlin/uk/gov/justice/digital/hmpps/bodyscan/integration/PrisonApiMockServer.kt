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
  fun stubGetPersonalCareNeedsForPrisonNumbers(type: String, fromStartDate: LocalDate, toStartDate: LocalDate) {
    stubFor(
      post(
        "/api/bookings/offenderNo/personal-care-needs/count?type=" +
          "$type" +
          "&fromStartDate=${fromStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}" +
          "&toStartDate=${toStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
      )
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
    }
]"""
            )
        )
    )
  }

  fun stubGetSentenceDetails(prisonNumber: String, statusCode: Int) {
    stubFor(
      get("/api/offenders/$prisonNumber/sentences")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(statusCode)
            .withBody(
              """{
    "bookingId": 1202691,
    "offenderNo": "$prisonNumber",
    "firstName": "GEOFF",
    "lastName": "BROWN",
    "agencyLocationId": "LII",
    "mostRecentActiveBooking": true,
    "sentenceDetail": {
        "bookingId": 1202691
    },
    "dateOfBirth": "1992-03-12",
    "agencyLocationDesc": "LINCOLN (HMP)",
    "internalLocationDesc": "RECP"
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
