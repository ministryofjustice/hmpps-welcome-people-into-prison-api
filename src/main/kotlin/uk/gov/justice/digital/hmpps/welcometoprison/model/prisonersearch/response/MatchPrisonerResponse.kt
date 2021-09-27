package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@JsonInclude(NON_NULL)
data class MatchPrisonerResponse(
  val prisonerNumber: String?,
  val pncNumber: String?
)
