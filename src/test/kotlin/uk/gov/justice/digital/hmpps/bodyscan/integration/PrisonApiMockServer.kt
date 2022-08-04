package uk.gov.justice.digital.hmpps.bodyscan.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalToJson
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
}
