package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.welcometoprison.model.RepositoryTest
import java.time.LocalDate
import java.time.LocalDateTime

class ConfirmedArrivalRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: ConfirmedArrivalRepository

  @Test
  fun `can insert confirmed arrival record`() {
    val confirmedArrival = confirmedArrival()

    val bookingDb = repository.save(confirmedArrival())
    TestTransaction.flagForCommit()
    TestTransaction.end()

    Assertions.assertThat(confirmedArrival.id).isNull()
    Assertions.assertThat(bookingDb).isNotNull
    Assertions.assertThat(bookingDb.prisonNumber).isEqualTo(confirmedArrival.prisonNumber)
    Assertions.assertThat(bookingDb.movementId).isEqualTo(confirmedArrival.movementId)
    Assertions.assertThat(bookingDb.timestamp).isEqualTo(confirmedArrival.timestamp)
    Assertions.assertThat(bookingDb.prisonId).isEqualTo(confirmedArrival.prisonId)
    Assertions.assertThat(bookingDb.bookingId).isEqualTo(confirmedArrival.bookingId)
    Assertions.assertThat(bookingDb.arrivalDate).isEqualTo(confirmedArrival.arrivalDate)
  }

  @Test
  @Sql("classpath:repository/confirmed-arrival.sql")
  fun `find all by confirmed arrival date and prison id`() {
    val date = LocalDate.of(2020, 1, 1)
    val prisonId = "MDI"
    val bookings = repository.findAllByArrivalDateAndPrisonId(date, prisonId)
    Assertions.assertThat(bookings).hasSize(1)
  }

  companion object {
    private val TIMESTAMP_NOW: LocalDateTime = LocalDateTime.now()
    private val ARRIVAL_DATE: LocalDate = TIMESTAMP_NOW.toLocalDate()

    fun confirmedArrival() = ConfirmedArrival(
      id = null,
      prisonNumber = "Prison Number",
      movementId = "Movement Id",
      timestamp = TIMESTAMP_NOW,
      arrivalType = ArrivalType.NEW_TO_PRISON,
      prisonId = "Prison Id",
      bookingId = 123,
      arrivalDate = ARRIVAL_DATE,
    )
  }
}
