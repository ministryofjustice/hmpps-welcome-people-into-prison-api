package uk.gov.justice.digital.hmpps.bodyscan.model

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.config.NotFoundException

@Service
class BodyScanService(
  private val prisonApiClient: BodyScanPrisonApiClient,
) {

  fun getPrisonerImage(prisonNumber: String): ByteArray? =
    prisonApiClient.getPrisonerImage(prisonNumber)
      ?: throw NotFoundException("Could not find image for: '$prisonNumber'")
}
