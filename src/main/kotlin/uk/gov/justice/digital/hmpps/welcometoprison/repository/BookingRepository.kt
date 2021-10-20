package uk.gov.justice.digital.hmpps.welcometoprison.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.welcometoprison.model.Booking
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date


@Repository
interface BookingRepository : JpaRepository<Booking, Long> {
  @Query(
    "select b from Booking b where b.prisonId = :prisonId " +
      "AND b.movementId = :movementId " +
      "AND b.prisonerId = :prisonerId " +
      "AND b.bookingId = :bookingId " +
      "AND b.timestamp between :dateFrom and :dateTo "
  )
  fun findIfExistBetweenDates(
    prisonId: String,
    movementId: String,
    prisonerId: String,
    bookingId: String,
    dateFrom: LocalDateTime,
    dateTo: LocalDateTime
  ): List<Booking>


  fun findAllByBookingDateAndPrisonId(
    bookingDate: LocalDate,
    prisonId: String
  ): List<Booking>

}
