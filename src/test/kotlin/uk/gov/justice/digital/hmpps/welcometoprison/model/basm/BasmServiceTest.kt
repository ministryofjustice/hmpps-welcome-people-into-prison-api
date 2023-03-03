package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Gender
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.LocationType
import java.time.LocalDate

class BasmServiceTest {
  private val basmClient: BasmClient = mock()
  private val service = BasmService(basmClient)

  @Test
  fun `getMoves - happy path`() {
    whenever(basmClient.getPrison(any())).thenReturn(BasmTestData.PRISON)
    whenever(basmClient.getMovements(any(), any(), any())).thenReturn(BasmTestData.MOVEMENTS)

    val moves = service.getArrivals("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2))

    assertThat(moves).isEqualTo(
      listOf(
        Arrival(
          id = "476d47a3-013a-4772-94c7-5d043b0d0574",
          firstName = "Alexis Robert",
          lastName = "Jones",
          dateOfBirth = LocalDate.of(1996, 7, 23),
          prisonNumber = null,
          pncNumber = null,
          date = LocalDate.of(2021, 9, 29),
          fromLocation = "Penrith County Court",
          fromLocationId = "PENRCT",
          fromLocationType = LocationType.COURT,
          gender = Gender.MALE,
        ),
      ),
    )
  }

  @Test
  fun `getMovement - happy path`() {
    whenever(basmClient.getMovement(any())).thenReturn(BasmTestData.MOVEMENT)

    val moves = service.getArrival("testId")

    assertThat(moves).isEqualTo(
      Arrival(
        id = "6052fac2-ea13-408c-9786-02d0dc5e89ff",
        firstName = "Jason Harry",
        lastName = "Sims",
        dateOfBirth = LocalDate.of(1984, 1, 24),
        prisonNumber = null,
        pncNumber = null,
        date = LocalDate.of(2021, 9, 22),
        fromLocation = "Moorland (HMP & YOI)",
        fromLocationId = "MDI",
        fromLocationType = LocationType.OTHER,
      ),
    )
  }
}
