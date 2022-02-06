package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ConfirmCourtReturnResponse(
  val prisonNumber: String,
  val location: String?,
  val bookingId: Long
)
