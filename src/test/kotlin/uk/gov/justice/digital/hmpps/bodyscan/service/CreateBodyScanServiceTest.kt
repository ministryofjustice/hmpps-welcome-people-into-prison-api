package uk.gov.justice.digital.hmpps.bodyscan.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanPrisonApiClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareNeeds
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.SentenceDetails
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanDetailRequest
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanReason
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanResult
import uk.gov.justice.digital.hmpps.config.ClientException
import java.time.LocalDate

class CreateBodyScanServiceTest {
  private val bodyScanPrisonApiClient: BodyScanPrisonApiClient = mock()
  private val createBodyScanService = CreateBodyScanService(bodyScanPrisonApiClient)

  @Test
  fun `add body scan`() {
    whenever(
      bodyScanPrisonApiClient.getSentenceDetails(any())
    ).thenReturn(
      SentenceDetails(
        bookingId = 123L,
        offenderNo = "ADA",
        firstName = "Adam",
        lastName = "Brown",
        agencyLocationId = "LII",
        mostRecentActiveBooking = true,
        dateOfBirth = LocalDate.of(1977, 1, 1),
        agencyLocationDesc = null,
        internalLocationDesc = null
      )
    )

    createBodyScanService.addBodyScan(
      "ADA",
      BodyScanDetailRequest(
        date = LocalDate.of(2022, 1, 1),
        reason = BodyScanReason.INTELLIGENCE,
        result = BodyScanResult.NEGATIVE
      )
    )

    verify(bodyScanPrisonApiClient).addPersonalCareNeeds(
      123L,
      PersonalCareNeeds(
        LocalDate.of(2022, 1, 1),
        BodyScanReason.INTELLIGENCE,
        BodyScanResult.NEGATIVE
      )
    )
  }
  @Test
  fun `add body scan when sentence details not found`() {
    whenever(
      bodyScanPrisonApiClient.getSentenceDetails(any())
    ).thenThrow(ClientException::class.java)

    Assertions.assertThatExceptionOfType(ClientException::class.java).isThrownBy {
      createBodyScanService.addBodyScan(
        "ADA",
        BodyScanDetailRequest(
          date = LocalDate.of(2022, 1, 1),
          reason = BodyScanReason.INTELLIGENCE,
          result = BodyScanResult.NEGATIVE
        )
      )
    }
  }
}
