package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConfirmTemporaryAbsenceResponse(
  val prisonNumber: String,
  val location: String?,
)
