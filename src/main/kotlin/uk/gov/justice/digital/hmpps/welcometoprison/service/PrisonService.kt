package uk.gov.justice.digital.hmpps.welcometoprison.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.client.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.exception.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerImage

@Service
class PrisonService(@Autowired private val client: PrisonApiClient) {

  fun getPrisonerImage(offenderNumber: String): ByteArray? {
    val prisonerImage: PrisonerImage? =
      client.getPrisonerImages(offenderNumber)
        .filter { it.imageView.equals("FACE") && it.imageOrientation.equals("FRONT") }
        .maxByOrNull { it.captureDate }

    if (prisonerImage === null) throw NotFoundException("No front-facing image of the offenders face found for offender number: $offenderNumber")

    return client.getPrisonerImage(prisonerImage.imageId)
  }
}
