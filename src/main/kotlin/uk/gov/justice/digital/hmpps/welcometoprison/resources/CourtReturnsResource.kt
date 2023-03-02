package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ConfirmationService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnResponse

@RestController
@Validated
@RequestMapping(name = "Court returns", produces = [MediaType.APPLICATION_JSON_VALUE])
class CourtReturnsResource(
  private val confirmationService: ConfirmationService,
) {

  @PreAuthorize("hasRole('ROLE_BOOKING_CREATE') and hasRole('ROLE_TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
  @Operation(
    summary = "Confirm court return to prison id ",
    description = "Confirm court return for a specific prisoner to prison, role required is ROLE_BOOKING_CREATE and ROLE_TRANSFER_PRISONER, requires token associated with a username, scope = write",
    security = [
      SecurityRequirement(
        name = "ROLE_BOOKING_CREATE,ROLE_VIEW_ARRIVALS,ROLE_TRANSFER_PRISONER",
        scopes = ["write"],
      ),
    ],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Confirmation about successful court return",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ConfirmCourtReturnResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to retrieve",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unexpected error",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  @PostMapping(
    "/court-returns/{arrivalId}/confirm",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun confirmArrivalFromCourt(
    @PathVariable
    @Valid
    @NotEmpty
    arrivalId: String,

    @RequestBody
    @Valid
    @NotNull
    confirmCourtReturnRequest: ConfirmCourtReturnRequest,
  ): ConfirmCourtReturnResponse = confirmationService.confirmReturnFromCourt(arrivalId, confirmCourtReturnRequest)
}
