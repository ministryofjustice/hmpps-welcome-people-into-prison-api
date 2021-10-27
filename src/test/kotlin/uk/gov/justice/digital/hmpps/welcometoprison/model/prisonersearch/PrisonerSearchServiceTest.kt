package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.LocationType.CUSTODY_SUITE
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

class PrisonerSearchServiceTest {
  private val client: PrisonerSearchApiClient = mockk()
  private val service = PrisonerSearchService(client)

  @Test
  fun `happy path`() {
    every { client.matchPrisoner(any()) } returnsMany listOf(
      listOf(MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER, "ACTIVE IN")),
      listOf(MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER, INACTIVE_OUT))
    )

    val moves = service.getCandidateMatches(arrival)

    assertThat(moves).containsExactly(
      MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER, "ACTIVE IN"),
      MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER, INACTIVE_OUT)
    )
  }

  companion object {
    private val PRISON_NUMBER = "A1234AA"
    private val PNC_NUMBER = "1234/1234589A"

    private val arrival = Arrival(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1991, 7, 31),
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      date = LocalDate.of(2021, 1, 21),
      fromLocation = "MDI",
      fromLocationType = CUSTODY_SUITE,
      isCurrentPrisoner = false
    )
  }
}
