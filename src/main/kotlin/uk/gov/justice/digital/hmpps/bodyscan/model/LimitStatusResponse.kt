package uk.gov.justice.digital.hmpps.bodyscan.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Information how many body scans was done for person")
data class LimitStatusResponse(
  @Schema(description = "prison number", example = "G6081VQ")
  val prisonNumber: String,
  @Schema(description = "number of scans", example = "1")
  val numberOfBodyScans: Int
) {
  enum class BodyScanStatus {
    DO_NOT_SCAN, CLOSE_TO_LIMIT, OK_TO_SCAN
  }

  @Schema(description = "body scan status", example = "DO_NOT_SCAN")
  @JsonProperty("bodyScanStatus")
  fun getBodyScanStatus(): BodyScanStatus {
    return when {
      100 > numberOfBodyScans -> BodyScanStatus.OK_TO_SCAN
      115 < numberOfBodyScans && numberOfBodyScans > 99 -> BodyScanStatus.CLOSE_TO_LIMIT
      else -> BodyScanStatus.DO_NOT_SCAN
    }
  }
}
