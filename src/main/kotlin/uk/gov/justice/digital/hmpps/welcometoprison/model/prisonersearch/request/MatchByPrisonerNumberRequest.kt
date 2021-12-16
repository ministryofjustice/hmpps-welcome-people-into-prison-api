package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.request

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

@JsonInclude(NON_NULL)
data class MatchByPrisonerNumberRequest(
  val prisonerNumbers: List<String>,
)
