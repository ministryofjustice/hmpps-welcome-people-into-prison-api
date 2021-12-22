package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonInclude(NON_NULL)
@Schema(description = "A transfer into prison")
data class Transfer(
  @Schema(description = "First name", example = "Sam")
  val firstName: String,

  @Schema(description = "Last name", example = "Smith")
  val lastName: String,

  @Schema(description = "Date of birth", example = "1971-02-01")
  val dateOfBirth: LocalDate,

  @Schema(description = "Prison number", example = "A1234AA")
  val prisonNumber: String?,

  @Schema(description = "PNC number", example = "01/1234X")
  val pncNumber: String?,

  @Schema(description = "Date expected", example = "2020-02-23")
  val date: LocalDate,

  @Schema(description = "From Location", example = "Kingston-upon-Hull Crown Court")
  val fromLocation: String,
)
