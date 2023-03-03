package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ArrivalsService
import java.time.LocalDate

@RestController
@Validated
@RequestMapping(name = "Events", produces = [MediaType.APPLICATION_JSON_VALUE])
class EventsCsvResource(
  private val arrivalsService: ArrivalsService,
) {
  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Arrival events",
    description = "Arrival events in CSV format.",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
  )
  @GetMapping(path = ["/events"], produces = ["text/csv"])
  fun getEvents(
    @RequestParam(name = "start-date", required = true)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Parameter(description = "The earliest date for which to return event details.", required = true)
    startDate: LocalDate,

    @RequestParam(name = "days")
    @Parameter(description = "Return details of events occurring within this number of days of start-date")
    days: Long?,
  ) = arrivalsService.getArrivalsAsCsv(startDate, days ?: 7L)
}
