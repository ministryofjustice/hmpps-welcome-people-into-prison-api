package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class ConfirmedArrivalService(
  private val confirmedArrivalRepository: ConfirmedArrivalRepository,
) {
  fun extractConfirmedArrivalFromArrivals(agencyId: String, date: LocalDate, arrivals: List<Arrival>): List<Arrival> {
    val confirmedArrivals = confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(date, agencyId)
    return arrivals.filterNot { contains(it, confirmedArrivals) }
  }

  private fun contains(arrival: Arrival, confirmedArrivals: List<ConfirmedArrival>) =
    confirmedArrivals.stream().anyMatch {
      arrival.id == it.movementId
    }

  fun add(
    movementId: String,
    prisonNumber: String,
    prisonId: String,
    bookingId: Long,
    arrivalDate: LocalDate,
    arrivalType: ArrivalType
  ) {
    val confirmedArrival = ConfirmedArrival(
      null,
      prisonNumber,
      movementId,
      LocalDateTime.now(),
      arrivalType,
      prisonId,
      bookingId,
      arrivalDate
    )
    confirmedArrivalRepository.save(confirmedArrival)
  }
}
