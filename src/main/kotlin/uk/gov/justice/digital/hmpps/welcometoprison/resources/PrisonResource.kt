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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Prison
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferIn
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferInDetail
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@RestController
@Validated
class PrisonResource(
  private val prisonService: PrisonService,
) {
  @Operation(
    summary = "Retrieves latest front-facing image of an offenders face",
    description = "Retrieves latest front-facing image of an offenders face, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
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
  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @GetMapping(
    value = ["/prison/prisoner/{offenderNumber}/image", "/prisoner/{offenderNumber}/image"],
    produces = ["image/jpeg"]
  )
  fun getPrisonerImage(
    @Schema(description = "Offender Number", example = "A12345", required = true)
    @PathVariable offenderNumber: String
  ) = prisonService.getPrisonerImage(offenderNumber)

  @Operation(
    summary = "Retrieves prison info for a specific Prison ID",
    description = "Retrieves prison info for a specific Prison ID, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Returns information about a specific prison",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Prison::class)
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
        description = "No prison with prison ID found",
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
  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @GetMapping(value = ["/prison/{prisonId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getPrison(
    @Schema(description = "Prison ID", example = "MDI", required = true)
    @PathVariable prisonId: String
  ): PrisonView = PrisonView(prisonService.getPrison(prisonId).prisonName)

  @Operation(
    summary = " Completes transfer-in for a specific prisoner",
    description = "Completes transfer-in of prisoner, role required is TRANSFER_PRISONER",
    security = [SecurityRequirement(name = "ROLE_TRANSFER_PRISONER", scopes = ["write"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The prisoner transferred in",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = TransferIn::class))
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
        description = "Incorrect permissions to post",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "404",
        description = "Prisoner number not found",
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

  @PreAuthorize("hasRole('ROLE_TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
  @PostMapping(
    "/prison/prisoner/{prisonNumber}/transfer-in",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun transferIn(
    @PathVariable
    @Valid @NotEmpty
    prisonNumber: String,

    @RequestBody
    @Valid @NotNull
    transferInDetail: TransferInDetail
  ) {
    prisonService.transferInOffender(prisonNumber, transferInDetail)
  }
}

data class PrisonView(val description: String)
