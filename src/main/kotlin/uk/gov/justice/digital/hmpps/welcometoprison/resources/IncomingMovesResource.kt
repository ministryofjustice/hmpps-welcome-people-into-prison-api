package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.MovementService
import java.time.LocalDate

@RestController
@Validated
@RequestMapping(name = "Incoming moves", path = ["/incoming-moves"], produces = [MediaType.APPLICATION_JSON_VALUE])
class IncomingMovesResource(
  private val movementService: MovementService,
) {
  @PreAuthorize("hasRole('ROLE_VIEW_INCOMING_MOVEMENTS')")
  @Operation(
    summary = "Retrieves incoming movements for a specific prison",
    description = "Retrieves incoming movements for a specific prison, role required is ROLE_VIEW_INCOMING_MOVEMENTS",
    security = [SecurityRequirement(name = "ROLE_VIEW_INCOMING_MOVEMENTS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "A list of incoming movements for that agency",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Movement::class))
          )
        ]
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
        description = "Agency ID not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      )
    ]
  )
  @GetMapping("/{agencyId}")
  fun getMoves(
    @Schema(description = "Agency ID", example = "MDI", required = true)
    @PathVariable agencyId: String,
    @Parameter(description = "Movements on a specific date", example = "2020-01-26", required = true) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) @RequestParam date: LocalDate
  ) = movementService.getMovements(agencyId, date)
}
