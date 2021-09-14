package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import java.time.LocalDate

@Service
class MovementService(val basmService: BasmService) {
  fun getMovements(agencyId: String, date: LocalDate) = basmService.getMoves(agencyId, date, date)
}
