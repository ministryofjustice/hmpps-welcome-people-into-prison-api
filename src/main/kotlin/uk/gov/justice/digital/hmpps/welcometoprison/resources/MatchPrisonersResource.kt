package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.constraints.NotNull
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.PotentialMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest

@RestController
@Validated
class MatchPrisonersResource(
  private val prisonerSearchService: PrisonerSearchService,
) {
  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Retrieves list of potential prisoner matches",
    description = """Retrieves list of potential prisoner matches, role required is ROLE_VIEW_ARRIVALS
Will perform up to 3 different types of search:

 * by prison number - if provided
 * by pnc - if provided
 * by first name, last name and date of birth - if a minimum of last name and date of birth are provided.

Results will be ordered Prison number matches first, then any PNC matches, then any Name and Date of birth matches.
""",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "List of potential matches",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = PotentialMatch::class),
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
  @PostMapping(path = ["/match-prisoners"])
  fun matchPrisoners(
    @RequestBody
    @Valid
    @NotNull
    matchPrisonersRequest: MatchPrisonersRequest,
  ): List<PotentialMatch> = prisonerSearchService.findPotentialMatches(matchPrisonersRequest)
}
