package uk.gov.justice.digital.hmpps.welcometoprison.model.booking

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.welcometoprison.model.RepositoryTest
import java.time.LocalDate
import java.time.LocalDateTime

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
      bookingDate = LocalDate.now(),
    )
    val bookingDb = repository.save(booking.copy())
    TestTransaction.flagForCommit()
    TestTransaction.end()

    Assertions.assertThat(booking.id).isNull()
    Assertions.assertThat(bookingDb).isNotNull
    Assertions.assertThat(bookingDb.prisonId).isEqualTo(booking.prisonId)
    Assertions.assertThat(bookingDb.movementId).isEqualTo(booking.movementId)
    Assertions.assertThat(bookingDb.timestamp).isEqualTo(booking.timestamp)
    Assertions.assertThat(bookingDb.prisonerId).isEqualTo(booking.prisonerId)
    Assertions.assertThat(bookingDb.bookingId).isEqualTo(booking.bookingId)
    Assertions.assertThat(bookingDb.bookingDate).isEqualTo(booking.bookingDate)

  }

  @Test
  @Sql("classpath:repository/booking.sql")
  fun `find all by booking date and prison id`() {
    val date = LocalDate.of(2020, 1, 1)
    val prisonId = "prison id"
    val bookings = repository.findAllByBookingDateAndPrisonId(date, prisonId)
    Assertions.assertThat(bookings.size).isEqualTo(1)
  }
}
