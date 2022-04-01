package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalRepository
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest
import java.time.LocalDate

@Service
@Transactional
class ArrivalsService(
  private val basmService: BasmService,
  private val prisonerSearchService: PrisonerSearchService,
  private val confirmedArrivalRepository: ConfirmedArrivalRepository,
) {
  fun getArrival(moveId: String): Arrival = augmentWithPrisonData(basmService.getArrival(moveId))

  fun getArrivals(agencyId: String, date: LocalDate): List<Arrival> = basmService
    .getArrivals(agencyId, date, date)
    .filterNot(isConfirmedArrival(agencyId, date))
    .map { augmentWithPrisonData(it) }

  private fun isConfirmedArrival(agencyId: String, date: LocalDate): (Arrival) -> Boolean {
    val confirmedArrivals = confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(date, agencyId)
    return { confirmedArrivals.contains(it) }
  }

  private fun List<ConfirmedArrival>.contains(arrival: Arrival) = this.stream().anyMatch { arrival.id == it.movementId }

  private fun augmentWithPrisonData(arrival: Arrival): Arrival {
    val matches = prisonerSearchService.findPotentialMatches(
      MatchPrisonersRequest(
        arrival.firstName,
        arrival.lastName,
        arrival.dateOfBirth,
        arrival.prisonNumber,
        arrival.pncNumber
      )
    )

    return arrival.copy(
      isCurrentPrisoner = matches.isNotEmpty() && matches.all { it.isCurrentPrisoner },
      potentialMatches = matches
    )
  }
}
