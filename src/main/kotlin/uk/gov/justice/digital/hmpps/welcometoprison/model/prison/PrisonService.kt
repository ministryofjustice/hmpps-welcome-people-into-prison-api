package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.exception.NotFoundException

@Service
class PrisonService(@Autowired private val client: PrisonApiClient) {

  fun getPrisonerImage(offenderNumber: String): ByteArray? {
    val prisonerImage: PrisonerImage? =
      client.getPrisonerImages(offenderNumber)
        .filter { it.imageView.equals("FACE") && it.imageOrientation.equals("FRONT") }
        .maxByOrNull { it.captureDate }

    return prisonerImage?.let { client.getPrisonerImage(it.imageId) }
      ?: throw NotFoundException("No front-facing image of the offenders face found for offender number: $offenderNumber")
  }
}
