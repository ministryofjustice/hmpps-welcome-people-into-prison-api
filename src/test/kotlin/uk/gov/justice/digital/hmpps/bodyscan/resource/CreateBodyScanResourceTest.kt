package uk.gov.justice.digital.hmpps.bodyscan.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.bodyscan.integration.IntegrationTestBase

@Suppress("ClassName")
class CreateBodyScanResourceTest : IntegrationTestBase() {

  @Nested
  @DisplayName("Add body scan tests")
  inner class AddBodyScanTests {
    val VALID_REQUEST = """
        {
          "date": "1961-05-29",
          "reason": "INTELLIGENCE",
          "result": "POSITIVE"
        }
      """

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/body-scans/prisoners/1234")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.post().uri("/body-scans/prisoners/1234")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `happy path`() {
      val bookingId = 1234L

      webTestClient
        .post()
        .uri("/body-scans/prisoners/$bookingId")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(VALID_REQUEST)
        .exchange()
        .expectStatus().isNoContent

      // TODO verify call
    }

    @Test
    fun `add body scan validation failure`() {
      val bookingId = 1234L
      webTestClient
        .post()
        .uri("/body-scans/prisoners/$bookingId")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS"),
            scopes = listOf("read", "write")
          )
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          "{}"
        )
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().json(
          """
        {
          "status": 400,
          "errorCode": null,
          "moreInfo": null
        }
          """.trimIndent()
        )
    }
  }
}
