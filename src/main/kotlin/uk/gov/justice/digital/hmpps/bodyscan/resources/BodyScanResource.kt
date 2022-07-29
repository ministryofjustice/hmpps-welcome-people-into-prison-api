package uk.gov.justice.digital.hmpps.bodyscan.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanDetail
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanService
import uk.gov.justice.digital.hmpps.config.ErrorResponse
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@Validated
@RequestMapping(name = "Body Scan", produces = [MediaType.APPLICATION_JSON_VALUE])
class BodyScanResource(
  private val bodyScanService: BodyScanService,
) {
  @PreAuthorize("hasRole('ROLE_MAINTAIN_HEALTH_PROBLEMS')")
  @Operation(
    summary = "Add a body scan to a prisoner",
    description = "Add a body scan to a prisoner, role required is ROLE_MAINTAIN_HEALTH_PROBLEMS",
    security = [SecurityRequirement(name = "ROLE_MAINTAIN_HEALTH_PROBLEMS", scopes = ["write"])],
    responses = [
      ApiResponse(
        responseCode = "204",
        description = "Body scan successfully added",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to retrieve",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prison ID not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unexpected error",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
    ]
  )
  @PostMapping(path = ["/body-scan/prisoner/{prisonNumber}"])
  @ResponseStatus(HttpStatus.NO_CONTENT)
  fun addBodyScan(
    @Schema(description = "Booking Id", example = "1234", required = true)
    @PathVariable bookingId: Long,

    @RequestBody
    @Valid @NotNull
    detail: BodyScanDetail
  ): Unit = bodyScanService.addBodyScan(bookingId, detail)
}
