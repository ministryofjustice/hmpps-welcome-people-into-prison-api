package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.integration.BasmApiMockServer
import java.time.LocalDate

class BasmClientTest {

  private lateinit var basmClient: BasmClient

  companion object {
    @JvmField
    internal val mockServer = BasmApiMockServer()

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
    basmClient = BasmClient(webClient)
  }

  @Test
  fun `successful get prison`() {
    mockServer.stubGetPrison(200)
    val result = basmClient.getPrison("MDI")

    assertThat(result).isEqualTo(BasmTestData.PRISON)

    mockServer.verify(
      getRequestedFor(urlEqualTo("/api/reference/locations?filter%5Bnomis_agency_id%5D=MDI"))
    )
  }

  @Test
  fun `successful get movements`() {
    mockServer.stubGetMovements(200)
    val result = basmClient.getMovements(
      "a2bc2abf-75fe-4b7f-bf5a-a755bc290757",
      LocalDate.of(2017, 1, 2),
      LocalDate.of(2017, 1, 2)
    )

    assertThat(result).usingRecursiveComparison().isEqualTo(BasmTestData.MOVEMENTS)

    mockServer.verify(
      getRequestedFor(urlEqualTo("/api/moves?include=profile.person,from_location,to_location,person.gender&filter%5Bto_location_id%5D=a2bc2abf-75fe-4b7f-bf5a-a755bc290757&filter%5Bdate_from%5D=2017-01-02&filter%5Bdate_to%5D=2017-01-02&filter%5Bstatus%5D=requested,accepted,booked,in_transit,completed&page=1&per_page=200&sort%5Bby%5D=date&sort%5Bdirection%5D=asc"))
    )
  }

  @Test
  fun `successful get movement`() {
    mockServer.stubGetMovement("test", 200)
    val result = basmClient.getMovement("test")

    assertThat(result).usingRecursiveComparison().isEqualTo(BasmTestData.MOVEMENT)

    mockServer.verify(
      getRequestedFor(urlEqualTo("/api/moves/test?include=profile.person,from_location,to_location,person.gender"))
    )
  }
}
