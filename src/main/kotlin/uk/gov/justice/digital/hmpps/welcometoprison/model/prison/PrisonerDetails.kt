package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonInclude(NON_NULL)
@Schema(description = "A prisoner's details")
data class PrisonerDetails(
  @Schema(description = "Prisoner first name", example = "Jim")
  val firstName: String,

  @Schema(description = "Prisoner last name", example = "Smith")
  val lastName: String,

  @Schema(description = "Date of birth", example = "1970-12-25", format = "YYYY-DD-MM")
  val dateOfBirth: LocalDate,

  @Schema(description = "Prison number", example = "A1234BC")
  val prisonNumber: String,

  @Schema(description = "PNC number", example = "2018/0123456X")
  val pncNumber: String?,

  @Schema(description = "CRO number", example = "SF80/655108T")
  val croNumber: String?,

  @Schema(description = "Sex", example = "Female")
  val sex: String?,

  @Transient val isCurrentPrisoner: Boolean
)
