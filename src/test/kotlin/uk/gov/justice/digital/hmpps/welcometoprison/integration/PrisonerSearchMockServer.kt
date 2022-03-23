package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.http.HttpHeaders
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber

class PrisonerSearchMockServer : MockServer(8093) {
  private val mapper = ObjectMapper()

  fun stubMatchPrisoners(status: Int) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/match-prisoners\\\\?.*"
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
                        "gender": "Male"
                       }]"""
            )
        )
    )
  }

  fun stubGetPrisoner(status: Int) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/match-prisoners\\\\?.*"
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
                        "gender": "Male"
                       }]"""
            )
        )
    )
  }
  fun stubGetActivePrisoner(status: Int) {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/prisoner-search/match-prisoners\\\\?.*"
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
                        "status": "ACTIVE",
                        "gender": "Male"
                       }]"""
            )
        )
    )
  }

  fun stubMatchPrisonerByNameOneResult() {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/match-prisoners"
        )
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody(
              """{
  "matches": [
    {
      "prisoner": {
        "prisonerNumber": "A1234AA",
        "pncNumber": "12/394773H",
        "pncNumberCanonicalShort": "12/394773H",
        "pncNumberCanonicalLong": "2012/394773H",
        "croNumber": "29906/12J",
        "bookingId": "0001200924",
        "bookNumber": "38412A",
        "firstName": "ROBERT",
        "middleNames": "John James",
        "lastName": "LARSEN",
        "dateOfBirth": "1975-04-02",
        "gender": "Male",
        "ethnicity": "White: Eng./Welsh/Scot./N.Irish/British",
        "youthOffender": true,
        "maritalStatus": "Widowed",
        "religion": "Church of England (Anglican)",
        "nationality": "Egyptian",
        "status": "ACTIVE IN",
        "lastMovementTypeCode": "CRT",
        "lastMovementReasonCode": "CA",
        "inOutStatus": "IN",
        "prisonId": "MDI",
        "prisonName": "HMP Leeds",
        "cellLocation": "A-1-002",
        "aliases": [
          {
            "firstName": "Robert",
            "middleNames": "Trevor",
            "lastName": "Lorsen",
            "dateOfBirth": "1975-04-02",
            "gender": "Male",
            "ethnicity": "White : Irish"
          }
        ],
        "alerts": [
          {
            "alertType": "H",
            "alertCode": "HA",
            "active": true,
            "expired": true
          }
        ],
        "csra": "HIGH",
        "category": "C",
        "legalStatus": "SENTENCED",
        "imprisonmentStatus": "LIFE",
        "imprisonmentStatusDescription": "Serving Life Imprisonment",
        "mostSeriousOffence": "Robbery",
        "recall": false,
        "indeterminateSentence": true,
        "sentenceStartDate": "2020-04-03",
        "releaseDate": "2023-05-02",
        "confirmedReleaseDate": "2023-05-01",
        "sentenceExpiryDate": "2023-05-01",
        "licenceExpiryDate": "2023-05-01",
        "homeDetentionCurfewEligibilityDate": "2023-05-01",
        "homeDetentionCurfewActualDate": "2023-05-01",
        "homeDetentionCurfewEndDate": "2023-05-02",
        "topupSupervisionStartDate": "2023-04-29",
        "topupSupervisionExpiryDate": "2023-05-01",
        "additionalDaysAwarded": 10,
        "nonDtoReleaseDate": "2023-05-01",
        "nonDtoReleaseDateType": "ARD",
        "receptionDate": "2023-05-01",
        "paroleEligibilityDate": "2023-05-01",
        "automaticReleaseDate": "2023-05-01",
        "postRecallReleaseDate": "2023-05-01",
        "conditionalReleaseDate": "2023-05-01",
        "actualParoleDate": "2023-05-01",
        "tariffDate": "2023-05-01",
        "locationDescription": "Outside - released from Leeds",
        "restrictedPatient": true,
        "supportingPrisonId": "LEI",
        "dischargedHospitalId": "HAZLWD",
        "dischargedHospitalDescription": "Hazelwood House",
        "dischargeDate": "2020-05-01",
        "dischargeDetails": "Psychiatric Hospital Discharge to Hazelwood House"
      }
    }
  ],
  "matchedBy": "ALL_SUPPLIED"
}"""
            )
        )
    )
  }

  fun stubMatchPrisonerByNameZeroResult() {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/match-prisoners"
        )
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody(
              """{
  "matches": [
    
  ],
  "matchedBy": "ALL_SUPPLIED"
}"""
            )
        )
    )
  }

  fun stubMatchPrisonerByNameTwoResult() {
    stubFor(
      WireMock.post(
        WireMock.urlPathMatching(
          "/match-prisoners"
        )
      )
        .willReturn(
          WireMock.aResponse()
            .withHeaders(HttpHeaders(HttpHeader("Content-Type", "application/json")))
            .withStatus(200)
            .withBody(
              """{
  "matches": [
    {
      "prisoner": {
        "prisonerNumber": "A1234AA",
        "pncNumber": "12/394773H",
        "pncNumberCanonicalShort": "12/394773H",
        "pncNumberCanonicalLong": "2012/394773H",
        "croNumber": "29906/12J",
        "bookingId": "0001200924",
        "bookNumber": "38412A",
        "firstName": "Robert",
        "middleNames": "John James",
        "lastName": "Larsen",
        "dateOfBirth": "1975-04-02",
        "gender": "Female",
        "ethnicity": "White: Eng./Welsh/Scot./N.Irish/British",
        "youthOffender": true,
        "maritalStatus": "Widowed",
        "religion": "Church of England (Anglican)",
        "nationality": "Egyptian",
        "status": "ACTIVE IN",
        "lastMovementTypeCode": "CRT",
        "lastMovementReasonCode": "CA",
        "inOutStatus": "IN",
        "prisonId": "MDI",
        "prisonName": "HMP Leeds",
        "cellLocation": "A-1-002",
        "aliases": [
          {
            "firstName": "Robert",
            "middleNames": "Trevor",
            "lastName": "Lorsen",
            "dateOfBirth": "1975-04-02",
            "gender": "Male",
            "ethnicity": "White : Irish"
          }
        ],
        "alerts": [
          {
            "alertType": "H",
            "alertCode": "HA",
            "active": true,
            "expired": true
          }
        ],
        "csra": "HIGH",
        "category": "C",
        "legalStatus": "SENTENCED",
        "imprisonmentStatus": "LIFE",
        "imprisonmentStatusDescription": "Serving Life Imprisonment",
        "mostSeriousOffence": "Robbery",
        "recall": false,
        "indeterminateSentence": true,
        "sentenceStartDate": "2020-04-03",
        "releaseDate": "2023-05-02",
        "confirmedReleaseDate": "2023-05-01",
        "sentenceExpiryDate": "2023-05-01",
        "licenceExpiryDate": "2023-05-01",
        "homeDetentionCurfewEligibilityDate": "2023-05-01",
        "homeDetentionCurfewActualDate": "2023-05-01",
        "homeDetentionCurfewEndDate": "2023-05-02",
        "topupSupervisionStartDate": "2023-04-29",
        "topupSupervisionExpiryDate": "2023-05-01",
        "additionalDaysAwarded": 10,
        "nonDtoReleaseDate": "2023-05-01",
        "nonDtoReleaseDateType": "ARD",
        "receptionDate": "2023-05-01",
        "paroleEligibilityDate": "2023-05-01",
        "automaticReleaseDate": "2023-05-01",
        "postRecallReleaseDate": "2023-05-01",
        "conditionalReleaseDate": "2023-05-01",
        "actualParoleDate": "2023-05-01",
        "tariffDate": "2023-05-01",
        "locationDescription": "Outside - released from Leeds",
        "restrictedPatient": true,
        "supportingPrisonId": "LEI",
        "dischargedHospitalId": "HAZLWD",
        "dischargedHospitalDescription": "Hazelwood House",
        "dischargeDate": "2020-05-01",
        "dischargeDetails": "Psychiatric Hospital Discharge to Hazelwood House"
      }
    },
    {
      "prisoner": {
        "prisonerNumber": "A1233AA",
        "pncNumber": "12/394773H",
        "pncNumberCanonicalShort": "12/394773H",
        "pncNumberCanonicalLong": "2012/394773H",
        "croNumber": "29906/12J",
        "bookingId": "0001200924",
        "bookNumber": "38412A",
        "firstName": "Robert",
        "middleNames": "John James",
        "lastName": "Larsen",
        "dateOfBirth": "1975-04-02",
        "gender": "Female",
        "ethnicity": "White: Eng./Welsh/Scot./N.Irish/British",
        "youthOffender": true,
        "maritalStatus": "Widowed",
        "religion": "Church of England (Anglican)",
        "nationality": "Egyptian",
        "status": "ACTIVE IN",
        "lastMovementTypeCode": "CRT",
        "lastMovementReasonCode": "CA",
        "inOutStatus": "IN",
        "prisonId": "MDI",
        "prisonName": "HMP Leeds",
        "cellLocation": "A-1-002",
        "aliases": [
          {
            "firstName": "Robert",
            "middleNames": "Trevor",
            "lastName": "Lorsen",
            "dateOfBirth": "1975-04-02",
            "gender": "Male",
            "ethnicity": "White : Irish"
          }
        ],
        "alerts": [
          {
            "alertType": "H",
            "alertCode": "HA",
            "active": true,
            "expired": true
          }
        ],
        "csra": "HIGH",
        "category": "C",
        "legalStatus": "SENTENCED",
        "imprisonmentStatus": "LIFE",
        "imprisonmentStatusDescription": "Serving Life Imprisonment",
        "mostSeriousOffence": "Robbery",
        "recall": false,
        "indeterminateSentence": true,
        "sentenceStartDate": "2020-04-03",
        "releaseDate": "2023-05-02",
        "confirmedReleaseDate": "2023-05-01",
        "sentenceExpiryDate": "2023-05-01",
        "licenceExpiryDate": "2023-05-01",
        "homeDetentionCurfewEligibilityDate": "2023-05-01",
        "homeDetentionCurfewActualDate": "2023-05-01",
        "homeDetentionCurfewEndDate": "2023-05-02",
        "topupSupervisionStartDate": "2023-04-29",
        "topupSupervisionExpiryDate": "2023-05-01",
        "additionalDaysAwarded": 10,
        "nonDtoReleaseDate": "2023-05-01",
        "nonDtoReleaseDateType": "ARD",
        "receptionDate": "2023-05-01",
        "paroleEligibilityDate": "2023-05-01",
        "automaticReleaseDate": "2023-05-01",
        "postRecallReleaseDate": "2023-05-01",
        "conditionalReleaseDate": "2023-05-01",
        "actualParoleDate": "2023-05-01",
        "tariffDate": "2023-05-01",
        "locationDescription": "Outside - released from Leeds",
        "restrictedPatient": true,
        "supportingPrisonId": "LEI",
        "dischargedHospitalId": "HAZLWD",
        "dischargedHospitalDescription": "Hazelwood House",
        "dischargeDate": "2020-05-01",
        "dischargeDetails": "Psychiatric Hospital Discharge to Hazelwood House"
      }
    }
  ],
  "matchedBy": "ALL_SUPPLIED"
}"""
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
