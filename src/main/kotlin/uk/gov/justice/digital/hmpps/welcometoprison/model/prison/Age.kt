package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import java.time.LocalDate
import java.time.Period

object Age {
  fun lessThanTwentyOneYears(dateOfBirth: LocalDate, now: LocalDate) = Period.between(dateOfBirth, now).years < 21
}
