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
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchByPrisonerNumberRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonerRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.PrisonerAndPncNumber
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
          firstName = "JIM",
          lastName = "SMITH",
          dateOfBirth = LocalDate.of(1991, 7, 31),
          prisonerNumber = "A1278AA",
          pncNumber = "1234/1234589A",
          croNumber = "SF80/655108T",
          status = INACTIVE_OUT,
          gender = "Male"
        )
      )
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/match-prisoners"))
    )
  }

  @Test
  fun `successful match by prisoner number when PNC Number available`() {
    mockServer.stubMatchByPrisonerNumbers(
      200,
      listOf(PrisonerAndPncNumber(prisonerNumber = "A1278AA", pncNumber = "1234/1234589A"))
    )
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
        gender = "Male"
      )
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/prisoner-search/match-prisoners"))
    )
  }

  @Test
  fun `successful match by name and date of birth`() {
    mockServer.stubMatchPrisonerByNameAndDateOfBirthOneResult()
    val searchByNameAndDateOfBirth = SearchByNameAndDateOfBirth(
      lastName = "Larsen", firstName = "Robert",
      dateOfBirth = LocalDate.parse(
        "1975-04-02", DateTimeFormatter.ofPattern("yyyy-MM-dd")
      )
    )
    val result = client.matchPrisonerByNameAndDateOfBirth(searchByNameAndDateOfBirth)

    assertThat(result).isEqualTo(
      listOf(
        Prisoner(
          prisonerNumber = "A1234AA",
          pncNumber = "12/394773H",
          firstName = "ROBERT",
          lastName = "LARSEN",
          croNumber = "29906/12J",
          gender = "Male",
          dateOfBirth = LocalDate.parse(
            "1975-04-02", DateTimeFormatter.ofPattern("yyyy-MM-dd")
          )
        )
      )
    )

    mockServer.verify(
      postRequestedFor(urlEqualTo("/match-prisoners"))
    )
  }
}
