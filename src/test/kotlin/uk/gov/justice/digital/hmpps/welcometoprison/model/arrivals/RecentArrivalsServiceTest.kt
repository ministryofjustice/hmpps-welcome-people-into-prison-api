package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import java.time.LocalDate
import java.time.LocalTime

class RecentArrivalsServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()

  private val recentArrivalsService = RecentArrivalsService(prisonApiClient)

  @Test
  fun `recent arrival first page of 200 results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(200))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2),
      LocalDate.of(2020, 1, 2),
      50,
      0
    )
    val firstElement = arrivals.get().findFirst().get()
    val lastElement = arrivals.get().skip(arrivals.get().count() - 1).findFirst().get()

    assertThat(firstElement.prisonNumber).isEqualTo("AA000")
    assertThat(lastElement.prisonNumber).isEqualTo("AA0049")
    assertThat(arrivals.totalElements).isEqualTo(200)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(0)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  @Test
  fun `recent arrival second page of 200 results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(200))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2),
      LocalDate.of(2020, 1, 2),
      50,
      1
    )
    val firstElement = arrivals.get().findFirst().get()
    val lastElement = arrivals.get().skip(arrivals.get().count() - 1).findFirst().get()

    assertThat(firstElement.prisonNumber).isEqualTo("AA0050")
    assertThat(lastElement.prisonNumber).isEqualTo("AA0099")
    assertThat(arrivals.totalElements).isEqualTo(200)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(1)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  @Test
  fun `recent arrival last page of 199 results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(199))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2),
      LocalDate.of(2020, 1, 2),
      50,
      3
    )
    val firstElement = arrivals.get().findFirst().get()
    val lastElement = arrivals.get().skip(arrivals.get().count() - 1).findFirst().get()

    assertThat(firstElement.prisonNumber).isEqualTo("AA00150")
    assertThat(lastElement.prisonNumber).isEqualTo("AA00198")
    assertThat(arrivals.totalElements).isEqualTo(199)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(3)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  @Test
  fun `recent arrival get not existing page return 0 results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(20))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2),
      LocalDate.of(2020, 1, 2),
      50,
      1
    )

    assertThat(arrivals.count()).isEqualTo(0)
    assertThat(arrivals.totalElements).isEqualTo(20)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(1)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  @Test
  fun `recent arrival nor results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(0))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2),
      LocalDate.of(2020, 1, 2),
      50,
      0
    )

    assertThat(arrivals.count()).isEqualTo(0)
    assertThat(arrivals.totalElements).isEqualTo(0)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(0)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  private fun getList(size: Int): List<Movement> {
    val list = ArrayList<Movement>()
    repeat(size) { index ->
      list.add(
        Movement(
          offenderNo = "AA00".plus(index),
          bookingId = index.toLong(),
          dateOfBirth = LocalDate.ofEpochDay(index.toLong()),
          firstName = "firstName".plus(index),
          lastName = "lastName".plus(index),
          location = "location".plus(index),
          movementTime = LocalTime.of(12, 0, 0),
          movementDateTime = LocalDate.ofEpochDay(365 * 10 + index.toLong()).atStartOfDay()
        )
      )
    }
    return list
  }
}
