package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.context.transaction.TestTransaction
import uk.gov.justice.digital.hmpps.welcometoprison.model.RepositoryTest
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrivalRepository
import java.time.LocalDate
import java.time.LocalDateTime

class ConfirmedArrivalRepositoryTest : RepositoryTest() {

  @Autowired
  lateinit var repository: ConfirmedArrivalRepository

  @AfterEach
  fun afterEach() {
    repository.deleteAll()
  }

  @Test
  fun `can insert confirmed arrival record`() {
    val confirmedArrival = confirmedArrival()

    val persistedArrival = repository.save(confirmedArrival)
    TestTransaction.flagForCommit()
    TestTransaction.end()

    assertThat(persistedArrival.id).isNotNull
    assertThat(persistedArrival.prisonNumber).isEqualTo(confirmedArrival.prisonNumber)
    assertThat(persistedArrival.movementId).isEqualTo(confirmedArrival.movementId)
    assertThat(persistedArrival.timestamp).isEqualTo(confirmedArrival.timestamp)
    assertThat(persistedArrival.prisonId).isEqualTo(confirmedArrival.prisonId)
    assertThat(persistedArrival.bookingId).isEqualTo(confirmedArrival.bookingId)
    assertThat(persistedArrival.arrivalDate).isEqualTo(confirmedArrival.arrivalDate)
  }

  @Test
  @Sql("classpath:repository/confirmed-arrival.sql")
  fun `find all by confirmed arrival date and prison id`() {
    val date = LocalDate.of(2020, 1, 1)
    val prisonId = "MDI"
    val arrivals = repository.findAllByArrivalDateAndPrisonId(date, prisonId)
    assertThat(arrivals).hasSize(1)
  }
  @Test
  @Sql("classpath:repository/confirmed-arrival.sql")
  fun `find all between dates date and prison id`() {
    val fromDate = LocalDate.of(2020, 1, 2)
    val toDate = LocalDate.of(2020, 1, 4)
    val arrivals = repository.findAllByArrivalDateIsBetween(fromDate, toDate)
    assertThat(arrivals).hasSize(4)
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
      username = "USER-1"
    )
  }
}
