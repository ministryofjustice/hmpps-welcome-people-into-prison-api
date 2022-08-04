package uk.gov.justice.digital.hmpps.bodyscan.apiclient

import com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareCounter
import uk.gov.justice.digital.hmpps.bodyscan.integration.PrisonApiMockServer
import java.time.LocalDate

class PrisonApiClientTest {

  private lateinit var bodyScanPrisonApiClient: BodyScanPrisonApiClient

  companion object {
    @JvmField
    internal val mockServer = PrisonApiMockServer()

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
    bodyScanPrisonApiClient = BodyScanPrisonApiClient(webClient)
  }

  @Test
  fun `successful get personal care needs for prison numbers`() {
    val type = "BSCAN"
    val fromStartDate = LocalDate.of(2022, 1, 1)
    val toStartDate = LocalDate.of(2022, 12, 31)
    val prisonNumbers = listOf("G8266VG", "G8874VT", "G8874VU", "G8874VV", "G8874VW", "G8874VX", "G8874VY", "G8874VZ")
    mockServer.stubGetPersonalCareNeedsForPrisonNumbers(type, fromStartDate, toStartDate, prisonNumbers)
    val result =
      bodyScanPrisonApiClient.getPersonalCareNeedsForPrisonNumbers(type, fromStartDate, toStartDate, prisonNumbers)

    assertThat(result).containsExactly(
      PersonalCareCounter(offenderNo = "G8266VG", size = 1),
      PersonalCareCounter(offenderNo = "G8874VT", size = 24),
      PersonalCareCounter(offenderNo = "G8874VU", size = 0),
      PersonalCareCounter(offenderNo = "G8874VV", size = 99),
      PersonalCareCounter(offenderNo = "G8874VW", size = 100),
      PersonalCareCounter(offenderNo = "G8874VX", size = 115),
      PersonalCareCounter(offenderNo = "G8874VY", size = 116),
      PersonalCareCounter(offenderNo = "G8874VZ", size = 117)
    )
    mockServer.verify(
      postRequestedFor(urlEqualTo("/api/bookings/offenderNo/personal-care-needs/count?type=BSCAN&fromStartDate=2022-01-01&toStartDate=2022-12-31"))
    )
  }
}
