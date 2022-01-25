package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ArrivalsService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@RestController
@Validated
@RequestMapping(name = "Arrivals", produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtReturnsResource(
  private val arrivalsService: ArrivalsService
) {

  @PreAuthorize("hasRole('ROLE_BOOKING_CREATE') and hasRole('ROLE_TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
  @Operation(
    summary = "Confirms the arrival for a specific ID",
    description = "Confirms the arrival for a specific ID, role required is ROLE_BOOKING_CREATE and ROLE_TRANSFER_PRISONER, requires token associated with a username, scope = write",
    security = [SecurityRequirement(name = "ROLE_BOOKING_CREATE,ROLE_VIEW_ARRIVALS,ROLE_TRANSFER_PRISONER", scopes = ["write"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the created offender",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ConfirmArrivalResponse::class)
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

  @PostMapping(
    "/court-returns/{prisonNumber}/confirm",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun confirmArrivalFromCourt(
    @PathVariable
    @Valid @NotEmpty
    moveId: String,

    @RequestBody
    @Valid @NotNull
    confirmCourtReturnRequest: ConfirmCourtReturnRequest
  ): ConfirmArrivalResponse = arrivalsService.confirmArrivalFromCourt(moveId, confirmCourtReturnRequest)
}
