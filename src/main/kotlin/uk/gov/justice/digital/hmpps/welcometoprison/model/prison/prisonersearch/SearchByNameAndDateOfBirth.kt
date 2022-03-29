package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import java.time.LocalDate

data class SearchByNameAndDateOfBirth(
  val firstName: String?,
  val lastName: String,
  val dateOfBirth: LocalDate,
)
