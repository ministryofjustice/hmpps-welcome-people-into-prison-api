package uk.gov.justice.digital.hmpps.bodyscan.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

@Schema(description = "A request to create a new body scan")
data class BodyScanDetailRequest(

  @Schema(
    required = true,
    description = "Date on which the scan took place. Must be specified in YYYY-MM-DD format.",
    example = "1970-01-01",
  )
  @field:NotNull
  val date: LocalDate? = null,

  @Schema(required = true, description = "Reason for the scan", example = "INTELLIGENCE")
  @field:NotNull
  val reason: BodyScanReason? = null,

  @Schema(required = true, description = "Result of the scan", example = "POSITIVE")
  @field:NotNull
  val result: BodyScanResult? = null,

)

@Schema(description = "Reason for the scan")
enum class BodyScanReason(var desc: String) {
  INTELLIGENCE("intelligence"),
  REASONABLE_SUSPICION("reasonable suspicion"),
}

@Schema(description = "Result of a scan")
enum class BodyScanResult { POSITIVE, NEGATIVE }
