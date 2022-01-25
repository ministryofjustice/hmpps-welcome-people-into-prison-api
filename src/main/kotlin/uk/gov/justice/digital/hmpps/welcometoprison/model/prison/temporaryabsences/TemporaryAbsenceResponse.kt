package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(NON_NULL)
@Schema(description = "A temporary absence from a prison")
data class TemporaryAbsenceResponse(
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
  val reasonForAbsence: String,

  @Schema(description = "Date and time when prisoner left prison", example = "2022-01-18T14:00:00")
  val movementDateTime: LocalDateTime?
)
