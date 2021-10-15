package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import uk.gov.justice.digital.hmpps.welcometoprison.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService

@RestController
@Validated
@PreAuthorize("hasRole('ROLE_VIEW_INCOMING_MOVEMENTS')")
@RequestMapping(name = "Prison", path = ["/prison"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PrisonResource(
  private val prisonService: PrisonService,
) {
  @Operation(
    summary = "Retrieves latest front-facing image of an offenders face",
    description = "Retrieves latest front-facing image of an offenders face, role required is ROLE_VIEW_INCOMING_MOVEMENTS",
    security = [SecurityRequirement(name = "ROLE_VIEW_INCOMING_MOVEMENTS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Latest front-facing image of an offenders face",
        content = [
          Content(
            mediaType = "images/jpeg",
            array = ArraySchema(schema = Schema(implementation = Byte::class))
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
        description = "No front-facing image of the offenders face found",
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

  @GetMapping(value = ["/prisoner/{offenderNumber}/image"], produces = ["image/jpeg"])
  fun getPrisonerImage(
    @Schema(description = "Offender Number", example = "A12345", required = true)
    @PathVariable offenderNumber: String
  ) = prisonService.getPrisonerImage(offenderNumber)
}
