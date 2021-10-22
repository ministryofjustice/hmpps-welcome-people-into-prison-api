package uk.gov.justice.digital.hmpps.welcometoprison.model.booking

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import java.time.LocalDate

@Service
class BookingService(
  val bookingRepository: BookingRepository
) {
  fun extractExistingBookingsFromArrivals(agencyId: String, date: LocalDate, arrivals: List<Arrival>): List<Arrival> {
    val bookings = bookingRepository.findAllByBookingDateAndPrisonId(date, agencyId)
    return arrivals.filterNot { contains(it, bookings, date) }
  }

  private fun contains(arrival: Arrival, bookings: List<Booking>, bookingDate: LocalDate) = bookings.stream().anyMatch {
    it.bookingDate == bookingDate &&
      arrival.id == it.movementId &&
      arrival.prisonNumber == it.prisonId
    arrival.moveType == it.moveType
  }
}