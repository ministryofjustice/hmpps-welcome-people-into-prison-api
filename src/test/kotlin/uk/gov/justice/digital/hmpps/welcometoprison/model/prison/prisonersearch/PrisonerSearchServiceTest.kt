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
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerDetails
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
      listOf(MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, "ACTIVE IN")),
      listOf(MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, INACTIVE_OUT))
    )

    val moves = service.getCandidateMatches(arrival)

    assertThat(moves).containsExactly(
      MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, "ACTIVE IN"),
      MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, INACTIVE_OUT)
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

  @Test
  fun getPrisoner() {
    whenever(client.getPrisoner(any())).thenReturn(
      MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, INACTIVE_OUT)
    )

    val prisoner = service.getPrisoner("A1234AA")

    assertThat(prisoner).isEqualTo(
      PrisonerDetails(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER)
    )
  }

  companion object {
    private const val PRISON_NUMBER = "A1234AA"
    private const val PNC_NUMBER = "1234/1234589A"
    private const val CRO_NUMBER = "11/222222"
    private const val FIRST_NAME = "JIM"
    private const val LAST_NAME = "SMITH"
    private val DOB = LocalDate.of(1991, 7, 31)

    private val arrival = Arrival(
      id = "1",
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DOB,
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      date = LocalDate.of(2021, 1, 21),
      fromLocation = "MDI",
      fromLocationType = CUSTODY_SUITE,
      isCurrentPrisoner = false
    )
  }
}
