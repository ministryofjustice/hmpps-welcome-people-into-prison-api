package uk.gov.justice.digital.hmpps.bodyscan.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
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
}
