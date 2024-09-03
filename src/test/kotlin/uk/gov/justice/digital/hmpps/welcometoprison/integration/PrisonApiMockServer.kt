package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PrisonApiMockServer : WireMockServer(9005) {

  fun stubGetUserCaseLoads() {
    stubFor(
      get("/api/users/me/caseLoads")
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody("userCaseLoads".loadJson(this)),
        ),
    )
  }

  fun stubGetPrisonTransfersEnRoute(prisonId: String, movementDate: LocalDate) {
    stubFor(
      get("/api/movements/$prisonId/enroute?movementDate=" + movementDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
        .willReturn(
          aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody("prisonTransfersEnRoute".loadJson(this)),
        ),
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
                 "agencyId": "NMI",
                  "activeFlag": false,
                  "assignedLivingUnit": {
                    "agencyId": "NMI",
                    "locationId": 1,
                    "description": "RECP",
                    "agencyName": "Nottingham (HMP)"
                     
                  },
                  "physicalAttributes": {
                    "sexCode": "F",
                    "gender": "Female"
                  },
                  "identifiers": []
                }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubCreateOffenderWithoutAssignedLivingUnit(offenderNo: String) {
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
                 "agencyId": "NMI",
                  "activeFlag": false
                  },
                  "physicalAttributes": {
                    "sexCode": "F",
                    "gender": "Female"
                  },
                  "identifiers": []
                }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubCreateOffenderFails(status: Int) {
    stubFor(
      post("/api/offenders")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(status)
            .withBody("""{"message":"exception message"}"""),
        ),
    )
  }

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
}""",
            ),
        ),
    )
  }

  /* This scenario represents situation when Prison Api do not sent errorCode due to old code
   * We can remove it after  Prison Api Release
   * */
  fun stubCreateOffenderFailsPrisonerAlreadyExistWithoutErrorCode() {
    stubFor(
      post("/api/offenders")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(400)
            .withBody(
              """{
    "status": 400,
    "userMessage": "Prisoner with PNC 09/222376Z already exists with ID G9934UO",
    "developerMessage": "Prisoner with PNC 09/222376Z already exists with ID G9934UO"
}""",
            ),
        ),
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
              """.trimIndent(),
            ),
        ),
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
            """,
            ),
        ),
    )
  }

  fun stubAdmitOnNewBookingFailsNoCapacity() {
    stubFor(
      post("/api/offenders")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(409)
            .withBody(
              """
             {
    "status": 409,
    "errorCode" : 30001,
    "userMessage": "The cell MDI-RECP does not have any available capacity",
    "developerMessage": "The cell MDI-RECP does not have any available capacity"
}
            """,
            ),
        ),
    )
  }

  fun stubRecallOffender(offenderNo: String) {
    stubFor(
      put("/api/offenders/$offenderNo/recall")
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
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubRecallOffenderFails(offenderNo: String, status: Int) {
    stubFor(
      put("/api/offenders/$offenderNo/recall")
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
            """,
            ),
        ),
    )
  }

  fun stubTransferInOffender(offenderNo: String) {
    stubFor(
      put("/api/offenders/$offenderNo/transfer-in")
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
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubTransferInOffenderFails(offenderNo: String, status: Int) {
    stubFor(
      put("/api/offenders/$offenderNo/transfer-in")
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
            """,
            ),
        ),
    )
  }

  fun stubTransferInOffenderFailsNoCapacity(offenderNo: String) {
    stubFor(
      put("/api/offenders/$offenderNo/transfer-in")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(409)
            .withBody(
              """
             {
    "status": 409,
    "errorCode": 30001,
    "userMessage": "The cell MDI-RECP does not have any available capacity",
    "developerMessage": "The cell MDI-RECP does not have any available capacity"
}
            """,
            ),
        ),
    )
  }

  fun stubCourtTransferInOffender(offenderNo: String) {
    stubFor(
      put("/api/offenders/$offenderNo/court-transfer-in")
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
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubCourtTransferInOffenderFails(offenderNo: String, status: Int) {
    stubFor(
      put("/api/offenders/$offenderNo/court-transfer-in")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(status)
            .withBody(
              """
              {
                "status": $status,
                "userMessage": "No prisoner found for prisoner number A1234BC",
                "developerMessage": "No prisoner found for prisoner number A1234BC"
              }
            """,
            ),
        ),
    )
  }

  fun stubGetTemporaryAbsencesSuccess(agencyId: String) {
    stubFor(
      get("/api/movements/agency/$agencyId/temporary-absences")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
              [
                {
                  "offenderNo": "G1310UO",
                  "firstName": "EGURZTOF",
                  "lastName": "TOBONICA",
                  "dateOfBirth": "1990-10-15",
                  "movementTime": "2022-01-18T08:00:00",
                  "toCity": "City",
                  "toAgency": "ABRYMC",
                  "toAgencyDescription": "Aberystwyth Magistrates Court",
                  "movementReasonCode": "C6",
                  "movementReason": "Medical/Dental Inpatient Appointment",
                  "commentText": "pHnuWeNNnALpHnuWeNNnA"
                }
              ]
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetMainOffenceSuccess(bookingId: Long) {
    stubFor(
      get("/api/bookings/$bookingId/mainOffence")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
              [
                  {
                      "bookingId": $bookingId,
                      "offenceDescription": "Burglary dwelling and theft  - no violence",
                      "offenceCode": "TH68036",
                      "statuteCode": "TH68"
                  }
              ]
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetEmptyMainOffenceSuccess(bookingId: Long) {
    stubFor(
      get("/api/bookings/$bookingId/mainOffence")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
              [
              ]
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetMainOffenceNotFound(bookingId: Long) {
    stubFor(
      get("/api/bookings/$bookingId/mainOffence")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(404)
            .withBody(
              """
              {
                  "status": 404,
                  "userMessage": "Offender booking with id $bookingId not found.",
                  "developerMessage": "Offender booking with id $bookingId not found."
              }
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetImage404(offenderNumber: String) {
    stubFor(
      get("/api/bookings/offenderNo/$offenderNumber/image/data?fullSizeImage=false")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(404)
            .withBody(
              """
              {
                "status": 404,
                "userMessage": "Resource with id [$offenderNumber] not found.",
                "developerMessage": "Resource with id [$offenderNumber] not found."
              }
            """,
            ),

        ),
    )
  }

  fun stubGetImage(offenderNumber: String) {
    stubFor(
      get("/api/bookings/offenderNo/$offenderNumber/image/data?fullSizeImage=false")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.IMAGE_JPEG_VALUE)
            .withStatus(200)
            .withBodyFile("img/image.png"),
        ),
    )
  }

  fun stubGetTemporaryAbsencesWithTwoRecords(agencyId: String) {
    stubFor(
      get("/api/movements/agency/$agencyId/temporary-absences")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
              [
                {
                  "offenderNo": "A1234AA",
                  "firstName": "Jim",
                  "lastName": "Smith",
                  "dateOfBirth": "1991-07-31",
                  "movementTime": "2022-01-18T08:00:00",
                  "toCity": "City",
                  "toAgency": "ABRYMC",
                  "toAgencyDescription": "Aberystwyth Magistrates Court",
                  "movementReasonCode": "C6",
                  "movementReason": "Hospital",
                  "commentText": "pHnuWeNNnALpHnuWeNNnA"
                },
                {
                  "offenderNo": "A1278AA",
                  "firstName": "First",
                  "lastName": "Last",
                  "dateOfBirth": "1980-02-23",
                  "movementTime": "2022-01-18T08:00:00",
                  "toCity": "City",
                  "toAgency": "ABRYMC",
                  "toAgencyDescription": "Aberystwyth Magistrates Court",
                  "movementReasonCode": "C6",
                  "movementReason": "Dentist",
                  "commentText": "pHnuWeNNnALpHnuWeNNnA"
                }
              ]
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubConfirmTemporaryAbsencesSuccess(offenderNo: String) {
    stubFor(
      put("/api/offenders/$offenderNo/temporary-absence-arrival")
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
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubErrorTemporaryAbsencesSuccess(offenderNo: String, status: Int) {
    stubFor(
      put("/api/offenders/$offenderNo/temporary-absence-arrival")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(status)
            .withBody(
              """
              {
                "status": $status,
                "userMessage": "No prisoner found for prisoner number $offenderNo",
                "developerMessage": "No prisoner found for prisoner number $offenderNo"
              }
            """,
            ),
        ),
    )
  }

  fun stubGetMovementSuccess(agencyId: String, fromDate: LocalDateTime, toDate: LocalDateTime) {
    stubFor(
      get(
        "/api/movements/$agencyId/in?fromDateTime=${fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}" +
          "&toDateTime=${toDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
      )
        .withHeader("Page-Limit", equalTo("10000"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
              [
    {
        "offenderNo": "G5155VP",
        "bookingId": 788854,
        "dateOfBirth": "1966-04-05",
        "firstName": "Gideon",
        "lastName": "Herkimer",
        "fromAgencyId": "OUT",
        "fromAgencyDescription": "OUTSIDE",
        "toAgencyId": "MDI",
        "toAgencyDescription": "Moorland (HMP & YOI)",
        "movementTime": "07:08:00",
        "movementDateTime": "2021-07-15T07:08:00",
        "location": "MDI-1-3-004"
    },
    {
        "offenderNo": "A7925DY",
        "bookingId": 1201387,
        "dateOfBirth": "1997-05-06",
        "firstName": "Prisonerhfirstname",
        "lastName": "Prisonerhlastname",
        "fromAgencyId": "OUT",
        "fromAgencyDescription": "OUTSIDE",
        "toAgencyId": "MDI",
        "toAgencyDescription": "Moorland (HMP & YOI)",
        "movementTime": "14:15:27",
        "movementDateTime": "2021-08-04T14:15:27",
        "location": "MDI-RECV"
    }
]
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetMovementSuccessWithNoLocation(agencyId: String, fromDate: LocalDateTime, toDate: LocalDateTime) {
    stubFor(
      get(
        "/api/movements/$agencyId/in?fromDateTime=${fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}" +
          "&toDateTime=${toDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
      )
        .withHeader("Page-Limit", equalTo("10000"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
              [
    {
        "offenderNo": "G5155VP",
        "bookingId": 788854,
        "dateOfBirth": "1966-04-05",
        "firstName": "Gideon",
        "lastName": "Herkimer",
        "fromAgencyId": "OUT",
        "fromAgencyDescription": "OUTSIDE",
        "toAgencyId": "MDI",
        "toAgencyDescription": "Moorland (HMP & YOI)",
        "movementTime": "07:08:00",
        "movementDateTime": "2021-07-15T07:08:00",
        "location": "MDI-1-3-004"
    },
    {
        "offenderNo": "A7925DY",
        "bookingId": 1201387,
        "dateOfBirth": "1997-05-06",
        "firstName": "Prisonerhfirstname",
        "lastName": "Prisonerhlastname",
        "fromAgencyId": "OUT",
        "fromAgencyDescription": "OUTSIDE",
        "toAgencyId": "MDI",
        "toAgencyDescription": "Moorland (HMP & YOI)",
        "movementTime": "14:15:27",
        "movementDateTime": "2021-08-04T14:15:27"
    }
]
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetMovementSuccessButEmptyList(agencyId: String, fromDate: LocalDateTime, toDate: LocalDateTime) {
    stubFor(
      get(
        "/api/movements/$agencyId/in?fromDateTime=${fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}" +
          "&toDateTime=${toDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
      )
        .withHeader("Page-Limit", equalTo("10000"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """
              []
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetMovementEmptyListWhenServerError(agencyId: String, fromDate: LocalDateTime, toDate: LocalDateTime, status: Int) {
    stubFor(
      get(
        "/api/movements/$agencyId/in?fromDateTime=${fromDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}" +
          "&toDateTime=${toDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}",
      )
        .withHeader("Page-Limit", equalTo("10000"))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(status)
            .withBody(
              """
              []
              """.trimIndent(),
            ),
        ),
    )
  }

  fun stubGetAgencySuccess(agencyId: String) {
    stubFor(
      get("/api/agencies/$agencyId")
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withStatus(200)
            .withBody(
              """{"prisonName":"Nottingham (HMP)"}
              """.trimIndent(),
            ),
        ),
    )
  }
}
