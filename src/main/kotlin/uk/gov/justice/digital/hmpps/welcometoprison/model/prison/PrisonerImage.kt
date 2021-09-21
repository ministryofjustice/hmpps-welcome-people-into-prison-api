package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonInclude(NON_NULL)
@Schema(description = "A prisoner image details")
data class PrisonerImage(
  @Schema(description = "Capture date", example = "2021-09-14", format = "YYYY-DD-MM")
  val captureDate: LocalDate,
  @Schema(description = "Image ID", example = "1")
  val imageId: Int,
  @Schema(description = "Image orientation", example = "FRONT")
  val imageOrientation: String?,
  @Schema(description = "Image type", example = "OFF_BKG")
  val imageType: String?,
  @Schema(description = "Image view", example = "FACE")
  val imageView: String?,
  @Schema(description = "Object ID", example = "1")
  val objectId: Int?,
)
