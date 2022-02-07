package uk.gov.justice.digital.hmpps.welcometoprison.formatter

import org.springframework.stereotype.Component

@Component
class LocationFormatter {

  fun format(value: String?): String {
    return when (value) {
      "RECP" -> "Reception"
      null -> ""
      else -> value
    }
  }
}
