package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.integration.PrisonRegisterMockServer

class PrisonRegisterClientTest {

  private lateinit var prisonRegisterClient: PrisonRegisterClient

  companion object {
    const val PRISON_ID = "NMI"

    @JvmField
    internal val mockServer = PrisonRegisterMockServer()

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
    prisonRegisterClient = PrisonRegisterClient(webClient)
  }

  @Test
  fun `get agency`() {
    mockServer.stubGetPrison(PRISON_ID)

    val agency = prisonRegisterClient.getPrison(PRISON_ID)

    Assertions.assertThat(agency).isEqualTo(
      Prison(prisonName = "Nottingham (HMP)")
    )

    mockServer.verify(
      WireMock.getRequestedFor(WireMock.urlEqualTo("/prisons/id/$PRISON_ID"))
    )
  }

  @Test
  fun `get agency when missing`() {
    mockServer.stubGetPrison(PRISON_ID, HttpStatus.NOT_FOUND.value())

    val agency = prisonRegisterClient.getPrison(PRISON_ID)

    Assertions.assertThat(agency).isNull()
  }
}
