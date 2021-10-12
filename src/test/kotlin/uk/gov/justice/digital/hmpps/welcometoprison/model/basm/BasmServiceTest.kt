package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.LocationType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import java.time.LocalDate

class BasmServiceTest {
  private val basmClient: BasmClient = mockk()
  private val service = BasmService(basmClient)

  private val movement = Movement(
    id = "476d47a3-013a-4772-94c7-5d043b0d0574",
    firstName = "Alexis",
    lastName = "Jones",
    dateOfBirth = LocalDate.of(1996, 7, 23),
    prisonNumber = null,
    pncNumber = null,
    date = LocalDate.of(2021, 9, 29),
    fromLocation = "Penrith County Court",
    fromLocationType = LocationType.COURT
  )

  @Test
  fun `getMoves - happy path`() {
    every { basmClient.getPrison(any()) } returns BasmTestData.PRISON
    every { basmClient.getMovements(any(), any(), any()) } returns BasmTestData.MOVEMENTS

    val moves = service.getMoves("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2))

    assertThat(moves).isEqualTo(
      listOf(
        Movement(
          id = "476d47a3-013a-4772-94c7-5d043b0d0574",
          firstName = "Alexis",
          lastName = "Jones",
          dateOfBirth = LocalDate.of(1996, 7, 23),
          prisonNumber = null,
          pncNumber = null,
          date = LocalDate.of(2021, 9, 29),
          fromLocation = "Penrith County Court",
          fromLocationType = LocationType.COURT
        )
      )
    )
  }

  @Test
  fun `getMovement - happy path`() {
    every { basmClient.getMovement(any()) } returns BasmTestData.MOVEMENT

    val moves = service.getMovement("testId")

    assertThat(moves).isEqualTo(
      Movement(
        id = "6052fac2-ea13-408c-9786-02d0dc5e89ff",
        firstName = "Jason",
        lastName = "Sims",
        dateOfBirth = LocalDate.of(1984, 1, 24),
        prisonNumber = null,
        pncNumber = null,
        date = LocalDate.of(2021, 9, 22),
        fromLocation = "Moorland (HMP & YOI)",
        fromLocationType = LocationType.OTHER
      )
    )
  }
}
