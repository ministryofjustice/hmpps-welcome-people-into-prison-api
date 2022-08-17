package uk.gov.justice.digital.hmpps.bodyscan.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanPrisonApiClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareNeeds
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.toEventProperties
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanDetailRequest
import uk.gov.justice.digital.hmpps.config.SecurityUserContext

@Service
class CreateBodyScanService(
  private val bodyScanPrisonApiClient: BodyScanPrisonApiClient,
  private val telemetryClient: TelemetryClient,
  private val securityUserContext: SecurityUserContext
) {

  fun addBodyScan(prisonNumber: String, details: BodyScanDetailRequest) {
    val sentenceDetails = bodyScanPrisonApiClient.getOffenderDetails(prisonNumber)
    val personalCareNeeds = PersonalCareNeeds(
      date = details.date!!,
      bodyScanReason = details.reason!!,
      bodyScanResult = details.result!!
    )
    bodyScanPrisonApiClient.addPersonalCareNeeds(
      sentenceDetails.bookingId,
      personalCareNeeds
    )
    telemetryClient.trackEvent("BodyScan", personalCareNeeds.toEventProperties(prisonNumber, securityUserContext.principal), null)
  }
}
