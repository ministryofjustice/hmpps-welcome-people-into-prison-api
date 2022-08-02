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
    val prisonNumbers = listOf("G8266VG", "G8874VT")
    mockServer.stubGetPersonalCareNeedsForPrisonNumbers(type, fromStartDate, toStartDate, prisonNumbers)
    val result = bodyScanPrisonApiClient.getPersonalCareNeedsForPrisonNumbers(type, fromStartDate, toStartDate, prisonNumbers)

    assertThat(result).containsExactly(
      PersonalCareCounter("G8266VG", 1),
      PersonalCareCounter("G8874VT", 24)
    )
    mockServer.verify(
      postRequestedFor(urlEqualTo("/api/bookings/offenderNo/personal-care-needs/count?type=BSCAN&fromStartDate=2022-01-01&toStartDate=2022-12-31"))
    )
  }
}
