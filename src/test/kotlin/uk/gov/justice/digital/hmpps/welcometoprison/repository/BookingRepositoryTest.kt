package uk.gov.justice.digital.hmpps.welcometoprison.repository

import java.time.LocalDate
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.welcometoprison.model.Booking
import java.time.LocalDateTime
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MoveType


class BookingRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: BookingRepository

  @Test
  fun `can insert booking record`() {
    val booking = Booking(
      id = null,
      prisonId = "Prison Id",
      movementId = "Movement Id",
      timestamp = LocalDateTime.now(),
      moveType = "TEST",
      prisonerId = "Prisoner Id",
      bookingId = "Booking Id",
      bookingDate = LocalDate.now()
    )
    val id = repository.save(booking).id
    TestTransaction.flagForCommit()
    TestTransaction.end()
    Assertions.assertThat(id).isNotNull()
  }

  @Test
  @Sql("classpath:repository/booking.sql")
  fun `get data between dates`() {
    val dateFrom = LocalDateTime.of(2020, 1, 1, 0, 0, 0)
    val dateTo = LocalDateTime.of(2020, 1, 3, 23, 59, 59)
    val bookings = repository.findIfExistBetweenDates(
      "prison id 1",
      "movement id 1",
      "prisoner id 1",
      "booking id 1",
      dateFrom,
      dateTo
    )
    Assertions.assertThat(bookings.size).isEqualTo(2)
  }

  @Test
  @Sql("classpath:repository/booking.sql")
  fun `find all by booking date and prison id`() {
    val date = LocalDate.of(2020, 1, 1)
    val prisonId = "prisoner id"
//    val bookings = repository.findAllByBookingDateAndPrisonId(date, prisonId)
    val bookings = repository.findAll()
    Assertions.assertThat(bookings.size).isEqualTo(1)
  }
}
