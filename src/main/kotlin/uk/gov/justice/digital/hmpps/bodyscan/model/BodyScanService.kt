package uk.gov.justice.digital.hmpps.bodyscan.model

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.config.NotFoundException

@Service
class BodyScanService(
  private val prisonApiClient: BodyScanPrisonApiClient,
) {

  fun getPrisonerImage(prisonNumber: String): ByteArray? =
    prisonApiClient.getPrisonerImage(prisonNumber)
      ?: throw NotFoundException("Could not find image for: '$prisonNumber'")

  fun addBodyScan(bookingId: Long, detail: BodyScanDetail) {
    log.info("Received request to add body scan: '$detail' to booking: '$bookingId'")
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
