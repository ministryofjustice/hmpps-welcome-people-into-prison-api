package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import java.time.LocalDate

@Service
class ConfirmedArrivalService(
  val confirmedArrivalRepository: ConfirmedArrivalRepository
) {
  fun extractConfirmedArrivalFromArrivals(agencyId: String, date: LocalDate, arrivals: List<Arrival>): List<Arrival> {
    val confirmedArrivals = confirmedArrivalRepository.findAllByArrivalDateAndPrisonNumber(date, agencyId)
    return arrivals.filterNot { contains(it, confirmedArrivals) }
  }

  private fun contains(arrival: Arrival, confirmedArrivals: List<ConfirmedArrival>) = confirmedArrivals.stream().anyMatch {
    arrival.id == it.movementId
  }
}
