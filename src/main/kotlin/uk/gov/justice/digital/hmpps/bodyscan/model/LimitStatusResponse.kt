package uk.gov.justice.digital.hmpps.bodyscan.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

private const val MAX_NUMBER_OF_BODY_SCANS_PER_YEAR = 116

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Information how many body scans was done for person")
data class LimitStatusResponse(
  @Schema(description = "prison number", example = "G6081VQ")
  val prisonNumber: String,
  @Schema(description = "number of scans", example = "1")
  val numberOfBodyScans: Int,
) {
  enum class BodyScanStatus {
    DO_NOT_SCAN,
    CLOSE_TO_LIMIT,
    OK_TO_SCAN,
  }

  @Schema(description = "body scan status", example = "DO_NOT_SCAN")
  @JsonProperty("bodyScanStatus")
  fun getBodyScanStatus(): BodyScanStatus = when {
    numberOfBodyScans < 100 -> BodyScanStatus.OK_TO_SCAN
    numberOfBodyScans < MAX_NUMBER_OF_BODY_SCANS_PER_YEAR -> BodyScanStatus.CLOSE_TO_LIMIT
    else -> BodyScanStatus.DO_NOT_SCAN
  }

  @Schema(description = "Number of scans remaining this year", example = "123")
  @JsonProperty("numberOfBodyScansRemaining")
  fun getNumberOfBodyScansRemaining() = (MAX_NUMBER_OF_BODY_SCANS_PER_YEAR - numberOfBodyScans).coerceAtLeast(0)
}
