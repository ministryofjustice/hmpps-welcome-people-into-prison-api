package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.integration.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.PotentialMatchRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber
import java.time.LocalDate

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
    val result = client.matchPrisoner(PotentialMatchRequest(nomsNumber = "identifier"))

    assertThat(result).isEqualTo(
      listOf(
        MatchPrisonerResponse(
          firstName = "JIM",
          lastName = "SMITH",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonerNumber = "A1278AA",
          pncNumber = "1234/1234589A",
          croNumber = "SF80/655108T",
          status = INACTIVE_OUT,
          gender = "Male",
          prisonId = "MDI",
          lastMovementTypeCode = "REL",
        ),
      ),
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/possible-matches")),
    )
  }

  @Test
  fun `successful match by prisoner number when PNC Number available`() {
    mockServer.stubMatchByPrisonerNumbers(
      200,
      listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA", pncNumber = "1234/1234589A")),
    )
    val result = client.matchPncNumbersByPrisonerNumbers(listOf("A1278AA"))

    assertThat(result).isEqualTo(
      listOf(
        PrisonerAndPncNumber(
          prisonerNumber = "A1278AA",
          pncNumber = "1234/1234589A",
        ),
      ),
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/prisoner-numbers")),
    )
  }

  @Test
  fun `successful match by prisoner number when no PNC Number available`() {
    mockServer.stubMatchByPrisonerNumbers(200, listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA")))
    val result = client.matchPncNumbersByPrisonerNumbers(listOf("A1278AA"))

    assertThat(result).isEqualTo(
      listOf(
        PrisonerAndPncNumber(
          prisonerNumber = "A1278AA",
        ),
      ),
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/prisoner-numbers")),
    )
  }

  @Test
  fun `successfully get prisoner details`() {
    mockServer.stubGetPrisoner(200)
    val result = client.getPrisoner("A1278AA")

    assertThat(result).isEqualTo(

      MatchPrisonerResponse(
        firstName = "JIM",
        lastName = "SMITH",
        dateOfBirth = LocalDate.of(1991, 7, 31),
        prisonerNumber = "A1278AA",
        pncNumber = "1234/1234589A",
        croNumber = "SF80/655108T",
        status = "INACTIVE_OUT",
        gender = "Male",
        prisonId = "MDI",
        lastMovementTypeCode = "REL",
      ),
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/possible-matches")),
    )
  }

  @Test
  fun `successfully get prisoner details when no booking has been created`() {
    mockServer.stubGetPrisonerWithNoBooking(200)
    val result = client.getPrisoner("A1278AA")

    assertThat(result).isEqualTo(

      MatchPrisonerResponse(
        firstName = "EDUARDO",
        lastName = "STARK",
        dateOfBirth = LocalDate.of(1994, 1, 1),
        prisonerNumber = "A9205DY",
        pncNumber = null,
        croNumber = null,
        gender = "Male",
        status = null,
        lastMovementTypeCode = null,
        prisonId = null,
      ),
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/possible-matches")),
    )
  }
}
