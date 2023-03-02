package uk.gov.justice.digital.hmpps.bodyscan.model

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanPrisonApiClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.OffenderDetails
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareNeeds
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.toEventProperties
import uk.gov.justice.digital.hmpps.config.ClientException
import uk.gov.justice.digital.hmpps.config.SecurityUserContext
import java.time.LocalDate

class CreateBodyScanServiceTest {
  private val bodyScanPrisonApiClient: BodyScanPrisonApiClient = mock()
  private val telemetryClient: TelemetryClient = mock()
  private val securityUserContext: SecurityUserContext = mock()
  private val createBodyScanService =
    CreateBodyScanService(bodyScanPrisonApiClient, telemetryClient, securityUserContext)

  @Test
  fun `add body scan`() {
    val prisonNumber = "ADA"
    val bookingId = 123L
    val personalCareNeeds = PersonalCareNeeds(
      LocalDate.of(2022, 1, 1),
      BodyScanReason.INTELLIGENCE,
      BodyScanResult.NEGATIVE,
    )

    whenever(securityUserContext.principal).thenReturn("anonymous")

    whenever(
      bodyScanPrisonApiClient.getOffenderDetails(any()),
    ).thenReturn(
      OffenderDetails(
        bookingId = bookingId,
        offenderNo = prisonNumber,
        firstName = "Adam",
        lastName = "Brown",
        agencyId = "LII",
        activeFlag = true,
        dateOfBirth = LocalDate.of(1977, 1, 1),
      ),
    )

    createBodyScanService.addBodyScan(
      prisonNumber,
      BodyScanDetailRequest(
        date = LocalDate.of(2022, 1, 1),
        reason = BodyScanReason.INTELLIGENCE,
        result = BodyScanResult.NEGATIVE,
      ),
    )

    verify(bodyScanPrisonApiClient).addPersonalCareNeeds(
      bookingId,
      personalCareNeeds,
    )
    verify(telemetryClient).trackEvent("BodyScan", personalCareNeeds.toEventProperties(prisonNumber, "anonymous"), null)
  }

  @Test
  fun `add body scan when sentence details not found`() {
    whenever(
      bodyScanPrisonApiClient.getOffenderDetails(any()),
    ).thenThrow(ClientException::class.java)

    Assertions.assertThatExceptionOfType(ClientException::class.java).isThrownBy {
      createBodyScanService.addBodyScan(
        "ADA",
        BodyScanDetailRequest(
          date = LocalDate.of(2022, 1, 1),
          reason = BodyScanReason.INTELLIGENCE,
          result = BodyScanResult.NEGATIVE,
        ),
      )
    }
  }
}
