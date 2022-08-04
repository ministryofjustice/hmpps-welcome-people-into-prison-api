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
            scopes = listOf("read", "write")
          )
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
    val prisonNumbers = listOf("G8266VG", "G8874VT")

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
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .json(
          """
          [
            {"prisonNumber":"G8266VG","numberOfBodyScans":1,"bodyScanStatus":"OK_TO_SCAN"},
            {"prisonNumber":"G8874VT","numberOfBodyScans":24,"bodyScanStatus":"OK_TO_SCAN"}
          ]"""
        )
    }
  }
}
