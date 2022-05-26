package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import java.time.LocalDate
import java.time.LocalTime

class RecentArrivalsServiceTest {
  private val prisonApiClient: PrisonApiClient = mock()

  private val recentArrivalsService = RecentArrivalsService(prisonApiClient, LocationFormatter())

  @Test
  fun `location is formatted when contains RECP`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(listOf(testMovement(1, "NMP-RECP")))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2) to LocalDate.of(2020, 1, 2),
      PageRequest.of(0, 1)
    )

    assertThat(arrivals.content[0].location).isEqualTo("Reception")
  }

  @Test
  fun `location is unaltered when not containing RECP`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(listOf(testMovement(1, "Room-1")))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2) to LocalDate.of(2020, 1, 2),
      PageRequest.of(0, 1)
    )

    assertThat(arrivals.content[0].location).isEqualTo("Room-1")
  }

  @Test
  fun `recent arrival first page of 200 results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(200))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2) to LocalDate.of(2020, 1, 2),
      PageRequest.of(0, 50)
    )
    val firstElement = arrivals.get().findFirst().get()
    val lastElement = arrivals.get().skip(arrivals.get().count() - 1).findFirst().get()

    assertThat(firstElement.prisonNumber).isEqualTo("AA00000")
    assertThat(lastElement.prisonNumber).isEqualTo("AA00049")
    assertThat(arrivals.totalElements).isEqualTo(200)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(0)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  @Test
  fun `recent arrival second page of 200 results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(200))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2) to LocalDate.of(2020, 1, 2),
      PageRequest.of(1, 50)
    )
    val firstElement = arrivals.get().findFirst().get()
    val lastElement = arrivals.get().skip(arrivals.get().count() - 1).findFirst().get()

    assertThat(firstElement.prisonNumber).isEqualTo("AA00050")
    assertThat(lastElement.prisonNumber).isEqualTo("AA00099")
    assertThat(arrivals.totalElements).isEqualTo(200)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(1)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  @Test
  fun `recent arrival last page of 199 results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(199))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2) to LocalDate.of(2020, 1, 2),
      PageRequest.of(3, 50)
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
      LocalDate.of(2017, 1, 2) to LocalDate.of(2020, 1, 2),
      PageRequest.of(1, 50)
    )

    assertThat(arrivals.count()).isEqualTo(0)
    assertThat(arrivals.totalElements).isEqualTo(20)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(1)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  @Test
  fun `recent arrival no results`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(0))

    val arrivals = recentArrivalsService.getArrivals(
      "MDI",
      LocalDate.of(2017, 1, 2) to LocalDate.of(2020, 1, 2),
      PageRequest.of(0, 50)
    )

    assertThat(arrivals.count()).isEqualTo(0)
    assertThat(arrivals.totalElements).isEqualTo(0)
    assertThat(arrivals.pageable.pageNumber).isEqualTo(0)
    assertThat(arrivals.pageable.pageSize).isEqualTo(50)
  }

  @Test
  fun `fuzzy searching and pagination`() {

    whenever(prisonApiClient.getMovement(any(), any(), any())).thenReturn(getList(200))

    val getPage = { page: Int ->
      val arrivals = recentArrivalsService.getArrivals(
        "MDI",
        LocalDate.of(2017, 1, 2) to LocalDate.of(2020, 1, 2),
        PageRequest.of(page, 5),
        "AA01050"
      )
      assertThat(arrivals.count()).isEqualTo(arrivals.content.size)
      assertThat(arrivals.totalElements).isEqualTo(22)
      assertThat(arrivals.pageable.pageNumber).isEqualTo(page)
      assertThat(arrivals.pageable.pageSize).isEqualTo(5)
      arrivals.content.map { it.prisonNumber }
    }

    assertThat(getPage(0)).containsExactly("AA00050", "AA00000", "AA00010", "AA00020", "AA00030")
    assertThat(getPage(1)).containsExactly("AA00040", "AA00051", "AA00052", "AA00053", "AA00054")
    assertThat(getPage(2)).containsExactly("AA00055", "AA00056", "AA00057", "AA00058", "AA00059")
    assertThat(getPage(3)).containsExactly("AA00060", "AA00070", "AA00080", "AA00090", "AA00100")
    assertThat(getPage(4)).containsExactly("AA00105", "AA00150")
  }

  private fun getList(size: Int): List<Movement> {
    val list = ArrayList<Movement>()
    repeat(size) { index ->
      list.add(testMovement(index))
    }
    return list
  }

  private fun testMovement(
    index: Int,
    location: String = "location-$index"
  ): Movement {
    val number = index.toString().padStart(3, '0')
    return Movement(
      offenderNo = "AA00$number",
      bookingId = index.toLong(),
      dateOfBirth = LocalDate.ofEpochDay(index.toLong()),
      firstName = "firstName$number",
      lastName = "lastName$number",
      location = location,
      movementTime = LocalTime.of(12, 0, 0),
      movementDateTime = LocalDate.ofEpochDay(365 * 10 + index.toLong()).atStartOfDay()
    )
  }
}
