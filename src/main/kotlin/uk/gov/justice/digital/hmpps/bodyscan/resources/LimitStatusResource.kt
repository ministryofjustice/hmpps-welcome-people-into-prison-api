package uk.gov.justice.digital.hmpps.bodyscan.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.bodyscan.model.LimitStatusResponse
import uk.gov.justice.digital.hmpps.bodyscan.service.LimitStatusService
import uk.gov.justice.digital.hmpps.config.ErrorResponse
import java.time.Year

@RestController
@Validated
@RequestMapping(name = "Body Scan", produces = [MediaType.APPLICATION_JSON_VALUE])
class LimitStatusResource(
  private val limitStatusService: LimitStatusService,
) {
  @PreAuthorize("hasRole('ROLE_MAINTAIN_HEALTH_PROBLEMS')")
  @Operation(
    summary = "Get number of body scan to a prisoner",
    description = "Get number of body scan to a prisoner, role required is ROLE_MAINTAIN_HEALTH_PROBLEMS",
    security = [SecurityRequirement(name = "ROLE_MAINTAIN_HEALTH_PROBLEMS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information how many body scans was done for the person",
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
        description = "Prison Number not found",
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
  @GetMapping(path = ["/body-scans/prisoners/{prisonNumber}"])
  fun getBodyScanForSinglePrisoner(
    @Schema(description = "Prison Number", example = "G8874VT", required = true)
    @PathVariable prisonNumber: String
  ): LimitStatusResponse = limitStatusService.getLimitStatusForYearAndPrisonNumbers(Year.now(), listOf(prisonNumber)).first()
}
