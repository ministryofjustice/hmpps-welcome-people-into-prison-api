package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data for creating a search request to find matching prisoners")
data class MatchPrisonersRequest(

  @Schema(description = "First name", example = "Sam")
  val firstName: String?,

  @Schema(description = "Last name", example = "Smith")
  val lastName: String?,

  @Schema(description = "Date of birth", example = "1971-02-01")
  val dateOfBirth: LocalDate?,

  @Schema(description = "Prison number", example = "A1234AA")
  val prisonNumber: String? = null,

  @Schema(description = "PNC number", example = "01/1234X")
  val pncNumber: String? = null,
) {
  @Schema(hidden = true)
  fun isValid() = !(prisonNumber.isNullOrBlank()) || !(pncNumber.isNullOrBlank()) || !(lastName.isNullOrBlank() && dateOfBirth == null)
}
