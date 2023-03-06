package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import java.time.LocalDate

@JsonInclude(NON_NULL)
data class PotentialMatchRequest(
  val firstName: String? = null,

  val lastName: String? = null,

  val dateOfBirth: LocalDate? = null,

  val pncNumber: String? = null,

  val nomsNumber: String? = null,
)
