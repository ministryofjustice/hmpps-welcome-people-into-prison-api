package uk.gov.justice.digital.hmpps.welcometoprison.resources

import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.CreateAndAdmitOffenderDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.CreateOffenderResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
@RequestMapping(name = "Prison", path = ["/offenders"])
class OffenderResource(val prisonService: PrisonService) {

  @PostMapping(
    "/create-and-book-into-prison",
    consumes = [MediaType.APPLICATION_JSON_VALUE],
    produces = [MediaType.APPLICATION_JSON_VALUE]
  )
  @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
  fun createOffenderAndBookIntoPrison(
    @RequestBody
    @Valid
    createAndBookInmateDetail: CreateAndAdmitOffenderDetail
  ): CreateOffenderResponse = prisonService.createAndAdmitOffender(createAndBookInmateDetail)
}