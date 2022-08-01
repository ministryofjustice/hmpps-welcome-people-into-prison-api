package uk.gov.justice.digital.hmpps.bodyscan.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanPrisonApiClient
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanDetailRequest

@Service
class CreateBodyScanService(
  private val prisonApiClient: BodyScanPrisonApiClient,
) {

  fun addBodyScan(prisonNumber: String, detail: BodyScanDetailRequest) {
    log.info("Received request to add body scan: '$detail' to: '$prisonNumber'")
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
