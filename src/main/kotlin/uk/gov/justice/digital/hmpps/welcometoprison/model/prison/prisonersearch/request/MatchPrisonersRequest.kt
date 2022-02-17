package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data for creating a search request to find mating prisoners")
data class MatchPrisonersRequest(

  @Schema(description = "First name", example = "Sam")
  @field:NotNull
  val firstName: String,

  @Schema(description = "Last name", example = "Smith")
  @field:NotNull
  val lastName: String,

  @Schema(description = "Date of birth", example = "1971-02-01")
  @field:NotNull
  val dateOfBirth: LocalDate,

  @Schema(description = "Prison number", example = "A1234AA")
  val prisonNumber: String? = null,

  @Schema(description = "PNC number", example = "01/1234X")
  val pncNumber: String? = null
)
