package uk.gov.justice.digital.hmpps.welcometoprison.model

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import java.time.LocalDate

class MovementServiceTest {
  private val prisonService: PrisonService = mockk()
  private val basmService: BasmService = mockk()
  private val movementService = MovementService(basmService, prisonService)

  @Test
  fun `happy path`() {

    every { basmService.getMoves("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2)) } returns
      listOf(
        Movement(
          firstName = "JIM",
          lastName = "SMITH",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonNumber = "A1234AA",
          pncNumber = "99/123456J",
          date = LocalDate.of(2021, 1, 2),
          fromLocation = "MDI",
          moveType = MoveType.PRISON_TRANSFER
        )
      )

    every { prisonService.getMoves("MDI", LocalDate.of(2020, 1, 2)) } returns
      listOf(
        Movement(
          firstName = "First",
          lastName = "Last",
          dateOfBirth = LocalDate.of(1980, 2, 23),
          prisonNumber = "A1278AA",
          pncNumber = "1234/1234589A",
          date = LocalDate.of(2021, 1, 2),
          moveType = MoveType.PRISON_TRANSFER,
          fromLocation = "MDI"
        )
      )

    val moves = movementService.getMovements("MDI", LocalDate.of(2020, 1, 2))

    assertThat(moves).isEqualTo(
      listOf(
        Movement(
          firstName = "JIM",
          lastName = "SMITH",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonNumber = "A1234AA",
          pncNumber = "99/123456J",
          date = LocalDate.of(2021, 1, 2),
          fromLocation = "MDI",
          moveType = MoveType.PRISON_TRANSFER
        ),
        Movement(
          firstName = "First",
          lastName = "Last",
          dateOfBirth = LocalDate.of(1980, 2, 23),
          prisonNumber = "A1278AA",
          pncNumber = "1234/1234589A",
          date = LocalDate.of(2021, 1, 2),
          moveType = MoveType.PRISON_TRANSFER,
          fromLocation = "MDI"
        )
      )
    )
  }
}
