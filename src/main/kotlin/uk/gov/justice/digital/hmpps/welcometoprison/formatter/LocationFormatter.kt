package uk.gov.justice.digital.hmpps.welcometoprison.formatter

import org.springframework.stereotype.Component

@Component
class LocationFormatter {

  fun format(value: String): String {
    return when {
      value.contains("RECP") -> "Reception"
      else -> value
    }
  }
}
