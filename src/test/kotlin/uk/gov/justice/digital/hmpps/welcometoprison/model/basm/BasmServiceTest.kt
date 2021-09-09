package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.MoveType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import java.time.LocalDate

class BasmServiceTest {
  private val basmClient: BasmClient = mockk()
  private val service = BasmService(basmClient)

  @Test
  fun `happy path`() {
    every { basmClient.getPrison(any()) } returns BasmTestData.PRISON
    every { basmClient.getMovements(any(), any(), any()) } returns BasmTestData.MOVEMENTS

    val moves = service.getMoves("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2))

    assertThat(moves).isEqualTo(
      listOf(
        Movement(
          firstName = "JIM",
          lastName = "SMITH",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonNumber = "A1234AA",
          pncNumber = "99/123456J",
          date = LocalDate.of(2021, 4, 29),
          fromLocation = "BELMARSH (HMP)",
          moveType = MoveType.PRISON_TRANSFER
        )
      )
    )
  }
}
