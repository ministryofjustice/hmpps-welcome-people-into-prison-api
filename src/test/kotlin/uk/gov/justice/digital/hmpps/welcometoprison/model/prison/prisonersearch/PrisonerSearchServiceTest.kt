package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
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
  fun `findPotentialMatch from record`() {
    whenever(client.matchPrisoner(any())).thenReturn(
      listOf(
        MatchPrisonerResponse(
          prisonerNumber = PRISON_NUMBER,
          pncNumber = PNC_NUMBER,
          firstName = FIRST_NAME,
          lastName = LAST_NAME,
          dateOfBirth = DOB,
          croNumber = CRO_NUMBER,
          gender = GENDER,
          status = INACTIVE_OUT
        )
      )
    )
    val matchPrisonersRequest = MatchPrisonersRequest(
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DOB,
    )
    val result = service.findPotentialMatches(matchPrisonersRequest)

    assertThat(result.size).isEqualTo(1)
    with(result[0]) {
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
  fun getPrisoner() {
    whenever(client.getPrisoner(any())).thenReturn(
      MatchPrisonerResponse(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER, CRO_NUMBER, GENDER, INACTIVE_OUT)
    )

    val prisoner = service.getPrisoner("A1234AA")

    assertThat(prisoner).isEqualTo(
      PrisonerDetails(
        FIRST_NAME_FORMATTED,
        LAST_NAME_FORMATTED,
        DOB,
        PRISON_NUMBER,
        PNC_NUMBER,
        CRO_NUMBER,
        GENDER,
        false
      )
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
  }
}
