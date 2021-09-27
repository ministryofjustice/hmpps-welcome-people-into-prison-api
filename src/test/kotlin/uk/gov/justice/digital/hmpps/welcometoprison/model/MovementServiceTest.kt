package uk.gov.justice.digital.hmpps.welcometoprison.model

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.MoveType.PRISON_RECALL
import uk.gov.justice.digital.hmpps.welcometoprison.model.MoveType.PRISON_TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

class MovementServiceTest {
  private val prisonService: PrisonService = mockk()
  private val basmService: BasmService = mockk()
  private val prisonerSearchService: PrisonerSearchService = mockk()
  private val movementService = MovementService(basmService, prisonService, prisonerSearchService)

  private fun movementFactory(moveType: MoveType = PRISON_RECALL, prisonNumber: String? = "A1234AA", pncNumber: String? = "99/123456J"): Movement {
    return Movement(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1991, 7, 31),
      prisonNumber,
      pncNumber,
      date = LocalDate.of(2021, 1, 2),
      fromLocation = "MDI",
      moveType
    )
  }

  @Test
  fun `getMoves - happy path`() {
    every { basmService.getMoves("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2)) } returns
      listOf(
        Movement(
          id = "1",
          firstName = "JIM",
          lastName = "SMITH",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonNumber = "A1234AA",
          pncNumber = "99/123456J",
          date = LocalDate.of(2021, 1, 2),
          fromLocation = "MDI",
          moveType = PRISON_TRANSFER
        )
      )

    every { prisonService.getMoves("MDI", LocalDate.of(2020, 1, 2)) } returns
      listOf(
        Movement(
          id = "1",
          firstName = "First",
          lastName = "Last",
          dateOfBirth = LocalDate.of(1980, 2, 23),
          prisonNumber = "A1278AA",
          pncNumber = "1234/1234589A",
          date = LocalDate.of(2021, 1, 2),
          moveType = PRISON_TRANSFER,
          fromLocation = "MDI"
        )
      )

    val moves = movementService.getMovements("MDI", LocalDate.of(2020, 1, 2))

    assertThat(moves).isEqualTo(
      listOf(
        Movement(
          id = "1",
          firstName = "JIM",
          lastName = "SMITH",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonNumber = "A1234AA",
          pncNumber = "99/123456J",
          date = LocalDate.of(2021, 1, 2),
          fromLocation = "MDI",
          moveType = PRISON_TRANSFER
        ),
        Movement(
          id = "1",
          firstName = "First",
          lastName = "Last",
          dateOfBirth = LocalDate.of(1980, 2, 23),
          prisonNumber = "A1278AA",
          pncNumber = "1234/1234589A",
          date = LocalDate.of(2021, 1, 2),
          moveType = PRISON_TRANSFER,
          fromLocation = "MDI"
        )
      )
    )
  }

  @Test
  fun `getMovementsMatchedWithPrisoner - doesn't match PRISON_TRANSFER move types`() {
    val prisonTransferMovement = movementFactory(moveType = PRISON_TRANSFER)

    every { basmService.getMoves("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2)) } returns
      listOf(
        prisonTransferMovement
      )

    every { prisonService.getMoves("MDI", LocalDate.of(2020, 1, 2)) } returns
      listOf(
        prisonTransferMovement
      )

    val moves = movementService.getMovementsMatchedWithPrisoner("MDI", LocalDate.of(2020, 1, 2))

    verify(exactly = 0) { prisonerSearchService.matchPrisoner(any()) }
    assertThat(moves).isEqualTo(
      listOf(
        prisonTransferMovement,
        prisonTransferMovement
      )
    )
  }

  @Test
  fun `getMovementsMatchedWithPrisoner - adds Prison Number from PNC match`() {
    val movementWithoutPrisonNumber = movementFactory(prisonNumber = null, pncNumber = "testPncNumber")

    every { basmService.getMoves("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2)) } returns
      listOf(
        movementWithoutPrisonNumber
      )

    every { prisonService.getMoves("MDI", LocalDate.of(2020, 1, 2)) } returns emptyList()

    every { prisonerSearchService.matchPrisoner("testPncNumber") } returns listOf(MatchPrisonerResponse("testPrisonNumber", "testPncNumber"))

    val moves = movementService.getMovementsMatchedWithPrisoner("MDI", LocalDate.of(2020, 1, 2))

    verify(exactly = 1) { prisonerSearchService.matchPrisoner("testPncNumber") }
    assertThat(moves.get(0).prisonNumber).isEqualTo("testPrisonNumber")
  }

  @Test
  fun `getMovementsMatchedWithPrisoner - adds PNC from Prison Number match`() {
    val movementWithoutPnc = movementFactory(prisonNumber = "testPrisonNumber", pncNumber = null)

    every { basmService.getMoves("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2)) } returns
      listOf(
        movementWithoutPnc
      )

    every { prisonService.getMoves("MDI", LocalDate.of(2020, 1, 2)) } returns emptyList()

    every { prisonerSearchService.matchPrisoner("testPrisonNumber") } returns listOf(MatchPrisonerResponse("testPrisonNumber", "testPncNumber"))

    val moves = movementService.getMovementsMatchedWithPrisoner("MDI", LocalDate.of(2020, 1, 2))

    verify(exactly = 1) { prisonerSearchService.matchPrisoner("testPrisonNumber") }
    assertThat(moves.get(0).pncNumber).isEqualTo("testPncNumber")
  }

  @Test
  fun `getMovementsMatchedWithPrisoner - Prison Number null if no matches`() {
    val movementWithoutPnc = movementFactory(prisonNumber = "testPrisonNumber", pncNumber = "testPncNumber")

    every { basmService.getMoves("MDI", LocalDate.of(2020, 1, 2), LocalDate.of(2020, 1, 2)) } returns
      listOf(
        movementWithoutPnc
      )

    every { prisonService.getMoves("MDI", LocalDate.of(2020, 1, 2)) } returns emptyList()

    every { prisonerSearchService.matchPrisoner("testPrisonNumber") } returns emptyList()
    every { prisonerSearchService.matchPrisoner("testPncNumber") } returns emptyList()

    val moves = movementService.getMovementsMatchedWithPrisoner("MDI", LocalDate.of(2020, 1, 2))

    verify(exactly = 1) { prisonerSearchService.matchPrisoner("testPrisonNumber") }
    verify(exactly = 1) { prisonerSearchService.matchPrisoner("testPncNumber") }
    assertThat(moves.get(0).prisonNumber).isEqualTo(null)
  }
}
