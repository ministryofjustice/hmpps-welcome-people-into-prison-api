package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.LocationType
import java.time.LocalDate

class BasmServiceTest {
  private val basmClient: BasmClient = mockk()
  private val service = BasmService(basmClient)

  private val movement = Arrival(
    id = "476d47a3-013a-4772-94c7-5d043b0d0574",
    firstName = "Alexis",
    lastName = "Jones",
    dateOfBirth = LocalDate.of(1996, 7, 23),
    prisonNumber = null,
    pncNumber = null,
    date = LocalDate.of(2021, 9, 29),
    fromLocation = "Penrith County Court",
    fromLocationType = LocationType.COURT,
    moveType = "PRISON_REMAND"
  )

  @Test
  fun `getMoves - happy path`() {
    every { basmClient.getPrison(any()) } returns BasmTestData.PRISON
    every { basmClient.getMovements(any(), any(), any()) } returns BasmTestData.MOVEMENTS

    val moves = service.getArrivals("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2))

    assertThat(moves).isEqualTo(
      listOf(
        Arrival(
          id = "476d47a3-013a-4772-94c7-5d043b0d0574",
          firstName = "Alexis",
          lastName = "Jones",
          dateOfBirth = LocalDate.of(1996, 7, 23),
          prisonNumber = null,
          pncNumber = null,
          date = LocalDate.of(2021, 9, 29),
          fromLocation = "Penrith County Court",
          fromLocationType = LocationType.COURT,
          moveType = "PRISON_REMAND"
        )
      )
    )
  }

  @Test
  fun `getMovement - happy path`() {
    every { basmClient.getMovement(any()) } returns BasmTestData.MOVEMENT

    val moves = service.getArrival("testId")

    assertThat(moves).isEqualTo(
      Arrival(
        id = "6052fac2-ea13-408c-9786-02d0dc5e89ff",
        firstName = "Jason",
        lastName = "Sims",
        dateOfBirth = LocalDate.of(1984, 1, 24),
        prisonNumber = null,
        pncNumber = null,
        date = LocalDate.of(2021, 9, 22),
        fromLocation = "Moorland (HMP & YOI)",
        fromLocationType = LocationType.OTHER,
        moveType = "PRISON_REMAND"
      )
    )
  }
}
