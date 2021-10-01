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

  private val date = LocalDate.of(2021, 1, 2)

  private val basmMovement = Movement(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1991, 7, 31),
      prisonNumber =  "A1234AA",
      pncNumber = "99/123456J",
      date = date,
      fromLocation = "MDI",
      moveType = PRISON_RECALL
    )
  private val prisonServiceMovement = basmMovement.copy(
    id = "2",
    firstName = "First",
    lastName = "Last",
    dateOfBirth = LocalDate.of(1980, 2, 23),
    prisonNumber = "A1278AA",
    pncNumber = "1234/1234589A",
    moveType = PRISON_TRANSFER
  )



  @Test
  fun `getMoves - happy path`() {
    every { basmService.getMoves("MDI", date, date) } returns listOf(basmMovement)
    every { prisonService.getMoves("MDI", date) } returns listOf(prisonServiceMovement)

    val moves = movementService.getMovements("MDI", date)

    assertThat(moves).isEqualTo(listOf(basmMovement, prisonServiceMovement)
    )
  }

  @Test
  fun `getMovementsMatchedWithPrisoner - adds Prison Number from PNC match`() {
    val movementWithoutPrisonNumber = basmMovement.copy(prisonNumber = null, pncNumber = "testPncNumber")

    every { basmService.getMoves("MDI", date, date) } returns listOf(movementWithoutPrisonNumber)
    every { prisonService.getMoves("MDI", date) } returns emptyList()

    every { prisonerSearchService.matchPrisoner("testPncNumber") } returns listOf(MatchPrisonerResponse("testPrisonNumber", "testPncNumber"))

    val moves = movementService.getMovementsMatchedWithPrisoner("MDI", date)

    verify(exactly = 1) { prisonerSearchService.matchPrisoner("testPncNumber") }
    assertThat(moves[0].prisonNumber).isEqualTo("testPrisonNumber")
  }

  @Test
  fun `getMovementsMatchedWithPrisoner - adds PNC from Prison Number match`() {
    val movementWithoutPnc = basmMovement.copy(prisonNumber = "testPrisonNumber", pncNumber = null)

    every { basmService.getMoves("MDI", date, date) } returns listOf(movementWithoutPnc)
    every { prisonService.getMoves("MDI", date) } returns emptyList()

    every { prisonerSearchService.matchPrisoner("testPrisonNumber") } returns listOf(MatchPrisonerResponse("testPrisonNumber", "testPncNumber"))

    val moves = movementService.getMovementsMatchedWithPrisoner("MDI", date)

    verify(exactly = 1) { prisonerSearchService.matchPrisoner("testPrisonNumber") }
    assertThat(moves[0].pncNumber).isEqualTo("testPncNumber")
  }

  @Test
  fun `getMovementsMatchedWithPrisoner - Prison Number null if no matches`() {
    val movement = basmMovement.copy(prisonNumber = "testPrisonNumber", pncNumber = "testPncNumber")

    every { basmService.getMoves("MDI", date, date) } returns listOf(movement)
    every { prisonService.getMoves("MDI", date) } returns emptyList()

    every { prisonerSearchService.matchPrisoner("testPrisonNumber") } returns emptyList()
    every { prisonerSearchService.matchPrisoner("testPncNumber") } returns emptyList()

    val moves = movementService.getMovementsMatchedWithPrisoner("MDI", date)

    verify(exactly = 1) { prisonerSearchService.matchPrisoner("testPrisonNumber") }
    verify(exactly = 1) { prisonerSearchService.matchPrisoner("testPncNumber") }
    assertThat(moves[0].prisonNumber).isEqualTo(null)
  }

  @Test
  fun `getMovementsMatchedWithPrisoner - given Prison Number match returned and PNC Number match returned and PNC Numbers don't match between responses, Prison Number and PNC Number are null`() {
    val movement = basmMovement.copy(prisonNumber = "nonsensePrisonNumber", pncNumber = "nonsensePncNumber")

    every { basmService.getMoves("MDI", date, date) } returns listOf(movement)
    every { prisonService.getMoves("MDI", date) } returns emptyList()

    every { prisonerSearchService.matchPrisoner("nonsensePrisonNumber") } returns listOf(MatchPrisonerResponse("testPrisonNumber", "something"))
    every { prisonerSearchService.matchPrisoner("nonsensePncNumber") } returns listOf(MatchPrisonerResponse("testPrisonNumber", "somethingDifferent"))

    val moves = movementService.getMovementsMatchedWithPrisoner("MDI", date)

    verify(exactly = 1) { prisonerSearchService.matchPrisoner("nonsensePrisonNumber") }
    verify(exactly = 1) { prisonerSearchService.matchPrisoner("nonsensePncNumber") }
    assertThat(moves[0].prisonNumber).isEqualTo(null)
    assertThat(moves[0].pncNumber).isEqualTo(null)
  }
}
