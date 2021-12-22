package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL

const val INACTIVE_OUT = "INACTIVE OUT"

@JsonInclude(NON_NULL)
data class MatchPrisonerResponse(
  val prisonerNumber: String?,
  val pncNumber: String?,
  val status: String?
) {
  val isCurrentPrisoner: Boolean
    get() = status != null && status != INACTIVE_OUT
}
