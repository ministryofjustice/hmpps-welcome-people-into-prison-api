package uk.gov.justice.digital.hmpps.welcometoprison.resources

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
class LogResource(
  private val logService: LogService,

  ) {
  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @GetMapping(
    value = ["/log-test/{type}/{random}"],
    produces = [MediaType.APPLICATION_JSON_VALUE],
  )
  fun generateLog(
    @Schema(description = "log type", example = "info", required = true)
    @PathVariable
    type: String,
    @Schema(description = "random number", example = "1", required = true)
    @PathVariable
    random: Int,

    ) = logService.generateLog(type, random)
}
