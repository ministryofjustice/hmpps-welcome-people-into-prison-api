package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch

import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.integration.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.request.MatchByPrisonerNumberRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.PrisonerAndPncNumber

class PrisonerSearchApiClientTest {

  private lateinit var client: PrisonerSearchApiClient

  companion object {
    @JvmField
    internal val mockServer = PrisonerSearchMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      mockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      mockServer.stop()
    }
  }

  @BeforeEach
  fun resetStubs() {
    mockServer.resetAll()
    val webClient = WebClient.create("http://localhost:${mockServer.port()}")
    client = PrisonerSearchApiClient(webClient)
  }

  @Test
  fun `successful match prisoner`() {
    mockServer.stubMatchPrisoners(200)
    val result = client.matchPrisoner(MatchPrisonerRequest("identifier"))

    assertThat(result).isEqualTo(
      listOf(
        MatchPrisonerResponse(
          prisonerNumber = "A1278AA",
          pncNumber = "1234/1234589A",
          status = INACTIVE_OUT
        )
      )
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/match-prisoners"))
    )
  }

  @Test
  fun `successful match by prisoner number when PNC Number available`() {
    mockServer.stubMatchByPrisonerNumbers(200, listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA", pncNumber = "1234/1234589A")))
    val result = client.matchPncNumbersByPrisonerNumbers(MatchByPrisonerNumberRequest(listOf("A1278AA")))

    assertThat(result).isEqualTo(
      listOf(
        PrisonerAndPncNumber(
          prisonerNumber = "A1278AA",
          pncNumber = "1234/1234589A",
        )
      )
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/prisoner-numbers"))
    )
  }

  @Test
  fun `successful match by prisoner number when no PNC Number available`() {
    mockServer.stubMatchByPrisonerNumbers(200, listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA")))
    val result = client.matchPncNumbersByPrisonerNumbers(MatchByPrisonerNumberRequest(listOf("A1278AA")))

    assertThat(result).isEqualTo(
      listOf(
        PrisonerAndPncNumber(
          prisonerNumber = "A1278AA",
        )
      )
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/prisoner-numbers"))
    )
  }
}
