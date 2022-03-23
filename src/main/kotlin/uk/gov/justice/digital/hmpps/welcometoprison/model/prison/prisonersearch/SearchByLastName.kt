package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import java.time.LocalDate

data class SearchByLastName(
  val lastName: String,
  val firstName: String? = null,
  val dateOfBirth: LocalDate? = null,
  val pncNumber: String? = null,
  val nomsNumber: String? = null
)
