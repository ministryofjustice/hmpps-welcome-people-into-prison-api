package uk.gov.justice.digital.hmpps.bodyscan.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.bodyscan.integration.IntegrationTestBase
import java.time.LocalDate
import java.time.Year

@Suppress("ClassName")
class LimitStatusResourceTest : IntegrationTestBase() {

  @Nested
  inner class `Get single limit` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/body-scans/prisoners/1234")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/body-scans/prisoners/1234")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `happy path`() {
      val prisonNumber = "G8874VT"
      val startDate: LocalDate = LocalDate.ofYearDay(Year.now().value, 1)
      val endDate: LocalDate = LocalDate.ofYearDay(Year.now().value, startDate.lengthOfYear())
      prisonApiMockServer.stubGetPersonalCareNeedsForPrisonNumbers("BSCAN", startDate, endDate, listOf(prisonNumber))
      webTestClient
        .get()
        .uri("/body-scans/prisoners/$prisonNumber")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json("""{"prisonNumber":"G8874VT","numberOfBodyScans":24,"bodyScanStatus":"OK_TO_SCAN"}""")
    }
  }

  @Nested
  inner class `Get multiple results` {
    val prisonNumbers = listOf("G8874VU", "G8874VV", "G8874VW", "G8874VX", "G8874VY", "G8874VZ")

    @Test
    fun `requires authentication`() {
      webTestClient.post().uri("/body-scans/prisoners")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.post().uri("/body-scans/prisoners")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(prisonNumbers)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `happy path`() {
      val startDate: LocalDate = LocalDate.ofYearDay(Year.now().value, 1)
      val endDate: LocalDate = LocalDate.ofYearDay(Year.now().value, startDate.lengthOfYear())
      prisonApiMockServer.stubGetPersonalCareNeedsForPrisonNumbers("BSCAN", startDate, endDate, prisonNumbers)
      webTestClient
        .post()
        .uri("/body-scans/prisoners")
        .bodyValue(prisonNumbers)
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {"prisonNumber":"G8874VU","numberOfBodyScans":0,"bodyScanStatus":"OK_TO_SCAN", "numberOfBodyScansRemaining": 116},
            {"prisonNumber":"G8874VV","numberOfBodyScans":99,"bodyScanStatus":"OK_TO_SCAN", "numberOfBodyScansRemaining": 17},
            {"prisonNumber":"G8874VW","numberOfBodyScans":100,"bodyScanStatus":"CLOSE_TO_LIMIT", "numberOfBodyScansRemaining": 16},
            {"prisonNumber":"G8874VX","numberOfBodyScans":115,"bodyScanStatus":"CLOSE_TO_LIMIT", "numberOfBodyScansRemaining": 1},
            {"prisonNumber":"G8874VY","numberOfBodyScans":116,"bodyScanStatus":"DO_NOT_SCAN", "numberOfBodyScansRemaining": 0},
            {"prisonNumber":"G8874VZ","numberOfBodyScans":117,"bodyScanStatus":"DO_NOT_SCAN", "numberOfBodyScansRemaining": 0}
          ]""",
        )
    }
  }
}
