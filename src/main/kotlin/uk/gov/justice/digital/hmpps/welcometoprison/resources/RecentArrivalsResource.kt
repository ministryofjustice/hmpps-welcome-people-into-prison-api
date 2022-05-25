package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.RecentArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.RecentArrivalsService
import java.time.LocalDate

private interface ArrivalsPage : Page<RecentArrival>

@RestController
@Validated
@RequestMapping(name = "Recent Arrivals", produces = [MediaType.APPLICATION_JSON_VALUE])
class RecentArrivalsResource(private val recentArrivalsService: RecentArrivalsService) {
  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Recent arrivals for a specific prison",
    description = "Recent arrivals for a specific prison, role required is ROLE_VIEW_ARRIVALS",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "A list of recent incoming arrivals for that prison",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ArrivalsPage::class)
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
  @GetMapping(path = ["/prisons/{prisonId}/recent-arrivals"])
  fun getRecentArrivals(
    @Schema(description = "Prison ID", example = "MDI", required = true)
    @PathVariable prisonId: String,
    @Parameter(description = "Arrivals from specific date", example = "2020-01-26", required = true) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) @RequestParam(required = true) fromDate: LocalDate,
    @Parameter(description = "Arrivals to specific date", example = "2020-01-26", required = true) @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) @RequestParam toDate: LocalDate,
    @Parameter(description = "Size of the page", example = "50", required = false)
    @RequestParam(defaultValue = "50", required = false) pageSize: Int,
    @Parameter(description = "Page number to display", example = "0", required = false)
    @RequestParam(defaultValue = "0", required = false) page: Int,
    @Parameter(
      description = "Query to optionally filter moves, Performs complete/partial and fuzzy matching",
      example = "John Smith",
      required = false
    )
    @RequestParam(required = false) query: String?
  ): Page<RecentArrival> = recentArrivalsService.getArrivals(
    prisonId, fromDate to toDate, PageRequest.of(page, pageSize), query
  )
}
