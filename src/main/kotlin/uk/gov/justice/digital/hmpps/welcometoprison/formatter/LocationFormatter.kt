package uk.gov.justice.digital.hmpps.welcometoprison.formatter

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail

@Component
class LocationFormatter {

  fun extract(inmateDetail: InmateDetail): String =
    inmateDetail.assignedLivingUnit?.description?.let { format(it) }
      ?: throw IllegalArgumentException("Prisoner: '${inmateDetail.offenderNo}' does not have assigned living unit")

  fun format(value: String): String {
    return when {
      value.contains("RECP") -> "Reception"
      else -> value
    }
  }
}
