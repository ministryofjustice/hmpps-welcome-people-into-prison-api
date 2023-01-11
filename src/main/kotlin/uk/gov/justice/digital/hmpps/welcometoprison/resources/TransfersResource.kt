package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers.Transfer
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers.TransferInDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers.TransferResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers.TransfersService

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
  @GetMapping(
    path = [
      "/prisons/{prisonId}/transfers"
    ]
  )
  fun getTransfers(
    @Schema(description = "Prison ID", example = "MDI", required = true)
    @PathVariable prisonId: String,
  ): List<Transfer> = transfersService.getTransfers(prisonId)

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
  @GetMapping(
    path = [
      "/prisons/{prisonId}/transfers/{prisonNumber}",
    ]
  )
  fun getTransfer(
    @Schema(description = "Prison ID", example = "MDI", required = true)
    @PathVariable prisonId: String,
    @Schema(description = "Prison Number", example = "A1234AA", required = true)
    @PathVariable prisonNumber: String
  ): Transfer = transfersService.getTransfer(prisonId, prisonNumber)

  @PreAuthorize("hasRole('ROLE_TRANSFER_PRISONER') and hasAuthority('SCOPE_write')")
  @Operation(
    summary = " Completes transfer-in for a specific prisoner",
    description = "Completes transfer-in of prisoner, roles required are ROLE_TRANSFER_PRISONER, requires token associated with a username",
    security = [SecurityRequirement(name = "ROLE_TRANSFER_PRISONER", scopes = ["write"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "The prisoner transferred in",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = TransferResponse::class)
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
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  fun transferIn(
    @PathVariable
    @Valid @NotEmpty
    prisonNumber: String,

    @RequestBody
    @Valid
    @NotNull
    transferInDetail: TransferInDetail
  ): TransferResponse = transfersService.transferInOffender(prisonNumber, transferInDetail)
}
