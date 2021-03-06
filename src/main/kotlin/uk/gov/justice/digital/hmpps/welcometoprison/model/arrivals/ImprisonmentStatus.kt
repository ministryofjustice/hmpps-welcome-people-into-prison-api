package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "The imprisonment status")
data class ImprisonmentStatus(
  @Schema(description = "A unique code to refer to this status", example = "determinate-sentence")
  val code: String,

  @Schema(description = "The imprisonment status", example = "Sentenced - fixed length of time")
  val description: String,

  @Schema(description = "Associated Nomis code", example = "SENT")
  val imprisonmentStatusCode: String,

  @Schema(description = "Title for Movement reasons page, (if required)", example = "What is the type of fixed sentence?")
  val secondLevelTitle: String? = null,

  @Schema(description = "Validation message", example = "Select the type of the determinate sentence")
  val secondLevelValidationMessage: String? = null,

  @Schema(description = "Movement reasons data", example = "Intermittent custodial sentence, INTER ")
  val movementReasons: List<MovementReason>,
)
