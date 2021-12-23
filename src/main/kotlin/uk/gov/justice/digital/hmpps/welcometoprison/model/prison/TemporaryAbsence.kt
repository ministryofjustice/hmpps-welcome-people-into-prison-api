package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonInclude(NON_NULL)
@Schema(description = "A temporary absence from a prison")
data class TemporaryAbsence(
  @Schema(description = "First name", example = "Sam")
  val firstName: String,
  @Schema(description = "Last name", example = "Smith")
  val lastName: String,
  @Schema(description = "Date of birth", example = "1971-02-01")
  val dateOfBirth: LocalDate,
  @JsonAlias("offenderNo")
  @Schema(description = "Prison number", example = "A1234AA")
  val prisonNumber: String?,
  @JsonAlias("movementReason")
  @Schema(description = "Reason", example = "Medical/Dental Outpatient Appointment")
  val reasonForAbsence: String
)
