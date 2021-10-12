package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.LocationType.CUSTODY_SUITE
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

class PrisonerSearchServiceTest {
  private val client: PrisonerSearchApiClient = mockk()
  private val service = PrisonerSearchService(client)

  @Test
  fun `happy path`() {
    every { client.matchPrisoner(any()) } returnsMany listOf(
      listOf(MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER)),
      listOf(MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER))
    )

    val moves = service.getCandidateMatches(movement)

    assertThat(moves).isEqualTo(
      listOf(
        MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER), MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER)
      )
    )
  }

  companion object {
    private val PRISON_NUMBER = "A1234AA"
    private val PNC_NUMBER = "1234/1234589A"

    private val movement = Movement(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1991, 7, 31),
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      date = LocalDate.of(2021, 1, 21),
      fromLocation = "MDI",
      fromLocationType = CUSTODY_SUITE
    )
  }
}
