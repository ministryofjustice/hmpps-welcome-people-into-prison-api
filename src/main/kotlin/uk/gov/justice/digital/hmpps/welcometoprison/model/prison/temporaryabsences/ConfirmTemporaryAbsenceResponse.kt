package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Confirm temporary absence response")
data class ConfirmTemporaryAbsenceResponse(
  @Schema(description = "prison number", example = "G6081VQ")
  val prisonNumber: String,
  @Schema(description = "location", example = "D-3-017")
  val location: String,
) {
  @Deprecated("Please use prisonNumber instead", replaceWith = ReplaceWith("prisonNumber"))
  @Schema(description = "prison number", example = "G6081VQ")
  fun getOffenderNo(): String {
    return this.prisonNumber
  }
}
