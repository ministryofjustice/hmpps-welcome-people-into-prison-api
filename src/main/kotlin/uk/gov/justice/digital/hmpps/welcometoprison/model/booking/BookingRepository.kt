package uk.gov.justice.digital.hmpps.welcometoprison.model.booking

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface BookingRepository : JpaRepository<Booking, Long> {

  fun findAllByBookingDateAndPrisonId(
    bookingDate: LocalDate,
    prisonId: String
  ): List<Booking>
}
