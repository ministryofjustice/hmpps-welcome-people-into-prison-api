package uk.gov.justice.digital.hmpps.bodyscan.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import javax.validation.constraints.NotNull

@Schema(description = "A request to create a new body scan")
data class BodyScanDetailRequest(

  @Schema(
    required = true,
    description = "Date on which the scan took place. Must be specified in YYYY-MM-DD format.",
    example = "1970-01-01"
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

enum class BodyScanReason { INTELLIGENCE, REASONABLE_DOUBT }
enum class BodyScanResult { POSITIVE, NEGATIVE }
