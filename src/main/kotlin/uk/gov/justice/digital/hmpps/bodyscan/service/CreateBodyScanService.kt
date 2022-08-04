package uk.gov.justice.digital.hmpps.bodyscan.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanPrisonApiClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareNeeds
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanDetailRequest

@Service
class CreateBodyScanService(
  private val bodyScanPrisonApiClient: BodyScanPrisonApiClient,
) {

  fun addBodyScan(prisonNumber: String, details: BodyScanDetailRequest) {
    val sentenceDetails = bodyScanPrisonApiClient.getOffenderDetails(prisonNumber)
    bodyScanPrisonApiClient.addPersonalCareNeeds(
      sentenceDetails.bookingId,
      PersonalCareNeeds(
        date = details.date!!,
        bodyScanReason = details.reason!!,
        bodyScanResult = details.result!!
      )
    )
  }
}
