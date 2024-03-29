package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ArrivalsService
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ConfirmArrivalResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Confirmation
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ConfirmationService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import java.time.LocalDate

@RestController
@Validated
@RequestMapping(name = "Arrivals", produces = [MediaType.APPLICATION_JSON_VALUE])
class ArrivalsResource(
  private val arrivalsService: ArrivalsService,
  private val confirmationService: ConfirmationService,
) {
  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Produces arrivals for a specific prison",
    description = "Produces arrivals for a specific prison, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "A list of incoming arrivals for that prison",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Arrival::class)),
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
        responseCode = "404",
        description = "Prison ID not found",
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
  @GetMapping(path = ["/prisons/{prisonId}/arrivals"])
  fun getMoves(
    @Schema(description = "Prison ID", example = "MDI", required = true)
    @PathVariable
    prisonId: String,
    @Parameter(description = "Arrivals on a specific date", example = "2020-01-26", required = true)
    @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE,
    )
    @RequestParam
    date: LocalDate,
  ): List<Arrival> = arrivalsService.getArrivals(prisonId, date)

  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Retrieves the arrival for a specific ID",
    description = "Retrieves the arrival for a specific ID, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The arrival",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Arrival::class),
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
  @GetMapping(path = ["/arrivals/{arrivalId}"])
  fun getMove(
    @Schema(description = "ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    @PathVariable
    arrivalId: String,
  ): Arrival = arrivalsService.getArrival(arrivalId)

  @PreAuthorize("hasRole('ROLE_BOOKING_CREATE') and hasRole('ROLE_TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
  @Operation(
    summary = "Confirms the arrival for a specific ID",
    description = "Confirms the arrival for a specific ID, role required is ROLE_BOOKING_CREATE and ROLE_TRANSFER_PRISONER, requires token associated with a username, scope = write",
    security = [
      SecurityRequirement(
        name = "ROLE_BOOKING_CREATE,ROLE_VIEW_ARRIVALS,ROLE_TRANSFER_PRISONER",
        scopes = ["write"],
      ),
    ],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Information about the confirmed arrival",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ConfirmArrivalResponse::class),
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
    "/arrivals/{arrivalId}/confirm",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun confirmArrival(
    @PathVariable
    @Valid
    @NotEmpty
    arrivalId: String,
    @RequestBody
    @Valid
    @NotNull
    confirmArrivalDetail: ConfirmArrivalDetail,
  ): ConfirmArrivalResponse = confirmationService.confirmArrival(Confirmation.Expected(arrivalId, confirmArrivalDetail))
}
