package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.LocationType.CUSTODY_SUITE
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber
import java.time.LocalDate

class PrisonerSearchServiceTest {
  private val client: PrisonerSearchApiClient = mock()
  private val service = PrisonerSearchService(client)

  @Test
  fun `getCandidateMatches - happy path`() {
    whenever(client.matchPrisoner(any())).thenReturn(
      listOf(MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER, "ACTIVE IN")),
      listOf(MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER, INACTIVE_OUT))
    )

    val moves = service.getCandidateMatches(arrival)

    assertThat(moves).containsExactly(
      MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER, "ACTIVE IN"),
      MatchPrisonerResponse(PRISON_NUMBER, PNC_NUMBER, INACTIVE_OUT)
    )
  }

  @Test
  fun `getPncNumbers - happy path when PNC Number available`() {
    whenever(client.matchPncNumbersByPrisonerNumbers(any())).thenReturn(
      listOf(PrisonerAndPncNumber(PRISON_NUMBER, PNC_NUMBER))
    )
    val prisonerAndPncNumbers = service.getPncNumbers(listOf(PRISON_NUMBER))

    assertThat(prisonerAndPncNumbers).isEqualTo(mapOf(PRISON_NUMBER to PNC_NUMBER))
  }

  @Test
  fun `getPncNumbers - happy path when no PNC Number available`() {
    whenever(client.matchPncNumbersByPrisonerNumbers(any())).thenReturn(listOf(PrisonerAndPncNumber(PRISON_NUMBER)))

    val prisonerAndPncNumbers = service.getPncNumbers(listOf(PRISON_NUMBER))

    assertThat(prisonerAndPncNumbers).isEqualTo(mapOf(PRISON_NUMBER to null))

    verify(client).matchPncNumbersByPrisonerNumbers(any())
  }

  @Test
  fun `getPncNumbers - search with no matches`() {
    val prisonerAndPncNumbers = service.getPncNumbers(emptyList())

    assertThat(prisonerAndPncNumbers).isEmpty()

    verifyNoMoreInteractions(client)
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
