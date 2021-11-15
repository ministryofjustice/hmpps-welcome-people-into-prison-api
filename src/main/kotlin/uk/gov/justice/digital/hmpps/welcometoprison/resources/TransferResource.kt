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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferIn
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferInDetail
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

@RestController
@Validated
@RequestMapping(name = "Transfers")
class TransferResource(
  private val prisonService: PrisonService,
) {
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
    "/transfer-in/{prisonNumber}",
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
