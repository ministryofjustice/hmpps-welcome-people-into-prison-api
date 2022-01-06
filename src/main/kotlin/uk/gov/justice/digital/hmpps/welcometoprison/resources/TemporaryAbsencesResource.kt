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
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.TemporaryAbsence
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.TemporaryAbsenceService

@RestController
@Validated
@PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
@RequestMapping(name = "Prison", path = ["/temporary-absences"], produces = [MediaType.APPLICATION_JSON_VALUE])
class TemporaryAbsencesResource(
  private val temporaryAbsenceService: TemporaryAbsenceService
) {
  @Operation(
    summary = "Retrieves the temporary absences for a prison",
    description = "Retrieves the current temporary absences information, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The temporary absences from a prison",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = TemporaryAbsence::class))
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
        description = "Temporary absence data not found",
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
  @GetMapping(value = ["/{agencyId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getTemporaryAbsences(
    @Schema(description = "AgencyId", example = "MDI", required = true)
    @PathVariable agencyId: String
  ) = temporaryAbsenceService.getTemporaryAbsences(agencyId)

  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Produces individual temporary absence for a prison",
    description = "Produces individual temporary absence, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "A temporary absence for a prison",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = TemporaryAbsence::class)
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
        description = "Temporary absence data not found",
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
  @GetMapping(path = ["/{agencyId}/{prisonNumber}"])
  fun getTemporaryAbsence(
    @Schema(description = "Prison ID", example = "MDI", required = true)
    @PathVariable agencyId: String,
    @Schema(description = "Prison Number", example = "A1234AA", required = true)
    @PathVariable prisonNumber: String
  ): TemporaryAbsence = temporaryAbsenceService.getTemporaryAbsence(agencyId, prisonNumber)
}
