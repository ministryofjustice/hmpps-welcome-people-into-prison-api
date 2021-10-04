package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse

class PrisonerSearchServiceTest {
  private val client: PrisonerSearchApiClient = mockk()
  private val service = PrisonerSearchService(client)

  @Test
  fun `happy path`() {
    every { client.matchPrisoner(any()) } returns listOf(MatchPrisonerResponse("testPrisonNumber", "testPncNumber"))

    val moves = service.matchPrisoner("testPrisonNumber")

    assertThat(moves).isEqualTo(
      listOf(
        MatchPrisonerResponse("testPrisonNumber", "testPncNumber")
      )
    )
  }
}
