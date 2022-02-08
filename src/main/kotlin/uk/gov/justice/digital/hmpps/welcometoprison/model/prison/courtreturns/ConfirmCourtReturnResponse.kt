package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Confirm court return response")
data class ConfirmCourtReturnResponse(
  @Schema(description = "prison number", example = "G6081VQ")
  val prisonNumber: String,
  @Schema(description = "location", example = "D-3-017")
  val location: String,
  @Schema(description = "booking Id", example = "472195")
  val bookingId: Long
) {
  @Deprecated("Please use prisonNumber instead", replaceWith = ReplaceWith("prisonNumber"))
  @Schema(description = "prison number", example = "G6081VQ")
  fun getOffenderNo(): String {
    return this.prisonNumber
  }
  @Deprecated("Please use prisonNumber instead", replaceWith = ReplaceWith("prisonNumber"))
  @Schema(description = "prison number", example = "G6081VQ")
  fun getPrisonerNumber(): String {
    return this.prisonNumber
  }
}
