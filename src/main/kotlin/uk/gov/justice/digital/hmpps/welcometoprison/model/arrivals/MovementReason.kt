package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "The reason for a movement into prison")
data class MovementReason(
  @Schema(description = "Reason for movement, (if required)", example = "Intermittent custodial sentence")
  val description: String? = null,

  @Schema(description = "Associated Nomis code", example = "INTER")
  val movementReasonCode: String,
)
