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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferIn
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers.Transfer
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers.TransferInDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers.TransfersService
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@RestController
@Validated
@RequestMapping(name = "Transfers", produces = [MediaType.APPLICATION_JSON_VALUE])
class TransfersResource(
  private val transfersService: TransfersService,
) {
  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Produces transfers for a specific prison",
    description = "Produces transfers for a specific prison, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "A list of incoming transfers for that prison",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Transfer::class))
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
  @GetMapping(path = ["/prisons/{agencyId}/transfers/enroute"])
  fun getTransfers(
    @Schema(description = "Prison ID", example = "MDI", required = true)
    @PathVariable agencyId: String,
  ): List<Transfer> = transfersService.getTransfers(agencyId)

  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Produces individual transfer",
    description = "Produces individual transfer, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "A transfer for a prison",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Transfer::class)
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
        description = "Prison or Transfer not found",
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
  @GetMapping(path = ["/prisons/{agencyId}/transfers/enroute/{prisonNumber}"])
  fun getTransfer(
    @Schema(description = "Prison ID", example = "MDI", required = true)
    @PathVariable agencyId: String,
    @Schema(description = "Prison Number", example = "A1234AA", required = true)
    @PathVariable prisonNumber: String
  ): Transfer = transfersService.getTransfer(agencyId, prisonNumber)

  @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
  @Operation(
    summary = " Completes transfer-in for a specific prisoner",
    description = "Completes transfer-in of prisoner, roles required are BOOKING_CREATE",
    security = [SecurityRequirement(name = "BOOKING_CREATE", scopes = ["write"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The prisoner transferred in",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = TransferIn::class)
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

  @PostMapping(
    "/transfers/{prisonNumber}/confirm",
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
    transfersService.transferInOffender(prisonNumber, transferInDetail)
  }
}
