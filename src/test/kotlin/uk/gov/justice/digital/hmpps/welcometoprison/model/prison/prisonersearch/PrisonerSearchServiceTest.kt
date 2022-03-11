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
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest
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
      listOf(
        MatchPrisonerResponse(
          FIRST_NAME,
          LAST_NAME,
          DOB,
          PRISON_NUMBER,
          PNC_NUMBER,
          CRO_NUMBER,
          GENDER,
          "ACTIVE IN"
        )
      ),
      listOf(
        MatchPrisonerResponse(
          FIRST_NAME,
          LAST_NAME,
          DOB,
          PRISON_NUMBER,
          PNC_NUMBER,
          CRO_NUMBER,
          GENDER,
          INACTIVE_OUT
        )
      )
    )

    val moves = service.getCandidateMatches(arrival)

    assertThat(moves).containsExactly(
      MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, GENDER, "ACTIVE IN"),
      MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, GENDER, INACTIVE_OUT)
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
  fun `findPotentialMatch from one record`() {
    whenever(client.matchPrisonerByNameAndDateOfBirth(any())).thenReturn(
      listOf(
        Prisoner(
          prisonerNumber = PRISON_NUMBER,
          pncNumber = PNC_NUMBER,
          firstName = FIRST_NAME,
          lastName = LAST_NAME,
          dateOfBirth = DOB,
          croNumber = CRO_NUMBER,
          gender = GENDER
        )
      )
    )
    val matchPrisonersRequest = MatchPrisonersRequest(
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DOB,
    )
    val potentialMatchList = service.findPotentialMatch(matchPrisonersRequest)

    assertThat(potentialMatchList.size).isEqualTo(1)
    with(potentialMatchList[0]) {
      assertThat(firstName).isEqualTo(FIRST_NAME_FORMATTED)
      assertThat(lastName).isEqualTo(LAST_NAME_FORMATTED)
      assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
      assertThat(dateOfBirth).isEqualTo(DOB)
      assertThat(pncNumber).isEqualTo(PNC_NUMBER)
      assertThat(CRO_NUMBER).isEqualTo(CRO_NUMBER)
      assertThat(GENDER).isEqualTo(GENDER)
    }
  }

  @Test
  fun `findPotentialMatch merge two records`() {
    whenever(client.matchPrisonerByNameAndDateOfBirth(any())).thenReturn(
      listOf(
        Prisoner(
          prisonerNumber = PRISON_NUMBER,
          pncNumber = PNC_NUMBER,
          firstName = FIRST_NAME,
          lastName = LAST_NAME,
          dateOfBirth = DOB,
          gender = GENDER,
          croNumber = CRO_NUMBER
        ),
        Prisoner(
          prisonerNumber = PRISON_NUMBER,
          pncNumber = PNC_NUMBER,
          firstName = FIRST_NAME,
          lastName = LAST_NAME,
          dateOfBirth = DOB,
          gender = GENDER,
          croNumber = CRO_NUMBER
        )
      )
    )
    val matchPrisonersRequest = MatchPrisonersRequest(
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DOB,
    )
    val potentialMatchList = service.findPotentialMatch(matchPrisonersRequest)

    assertThat(potentialMatchList.size).isEqualTo(1)
    with(potentialMatchList[0]) {
      assertThat(firstName).isEqualTo(FIRST_NAME_FORMATTED)
      assertThat(lastName).isEqualTo(LAST_NAME_FORMATTED)
      assertThat(prisonNumber).isEqualTo(PRISON_NUMBER)
      assertThat(dateOfBirth).isEqualTo(DOB)
      assertThat(pncNumber).isEqualTo(PNC_NUMBER)
    }
  }

  @Test
  fun `findPotentialMatch get two records when prison number is different`() {
    whenever(client.matchPrisonerByNameAndDateOfBirth(any())).thenReturn(
      listOf(
        Prisoner(
          prisonerNumber = PRISON_NUMBER,
          pncNumber = PNC_NUMBER,
          firstName = FIRST_NAME,
          lastName = LAST_NAME,
          dateOfBirth = DOB,
          gender = GENDER,
          croNumber = CRO_NUMBER
        ),
        Prisoner(
          prisonerNumber = "A1234AB",
          pncNumber = PNC_NUMBER,
          firstName = FIRST_NAME,
          lastName = LAST_NAME,
          dateOfBirth = DOB,
          gender = GENDER,
          croNumber = CRO_NUMBER
        )
      )
    )
    val matchPrisonersRequest = MatchPrisonersRequest(
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DOB,
    )
    val potentialMatchList = service.findPotentialMatch(matchPrisonersRequest)

    assertThat(potentialMatchList.size).isEqualTo(2)
    assertThat(potentialMatchList[0].prisonNumber).isEqualTo(PRISON_NUMBER)
    assertThat(potentialMatchList[1].prisonNumber).isEqualTo("A1234AB")
  }

  @Test
  fun getPrisoner() {
    whenever(client.getPrisoner(any())).thenReturn(
      MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, GENDER, INACTIVE_OUT)
    )

    val prisoner = service.getPrisoner("A1234AA")

    assertThat(prisoner).isEqualTo(
      PrisonerDetails(FIRST_NAME_FORMATTED, LAST_NAME_FORMATTED, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, false)
    )
  }

  companion object {
    private const val PRISON_NUMBER = "A1234AA"
    private const val PNC_NUMBER = "1234/1234589A"
    private const val CRO_NUMBER = "11/222222"
    private const val FIRST_NAME = "JIM"
    private const val FIRST_NAME_FORMATTED = "Jim"
    private const val GENDER = "Male"
    private const val LAST_NAME = "SMITH"
    private const val LAST_NAME_FORMATTED = "Smith"
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
