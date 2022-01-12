package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryAbsences

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConfirmTemporaryAbsenceResponse(
  val prisonerNumber: String,
)
