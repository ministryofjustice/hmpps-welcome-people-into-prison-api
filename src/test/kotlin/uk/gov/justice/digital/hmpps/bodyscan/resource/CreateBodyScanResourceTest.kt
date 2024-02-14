package uk.gov.justice.digital.hmpps.bodyscan.resource

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.bodyscan.integration.IntegrationTestBase
import java.time.LocalDate

@Suppress("ClassName")
class CreateBodyScanResourceTest : IntegrationTestBase() {

  @Nested
  @DisplayName("Add body scan tests")
  inner class AddBodyScanTests {
    private val validRequest = """
        {
          "date": "2022-01-01",
          "reason": "REASONABLE_SUSPICION",
          "result": "POSITIVE"
        }
      """

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/body-scans/prisoners/A1278AA")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.post().uri("/body-scans/prisoners/A1278AA")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(validRequest)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `happy path`() {
      val prisonNumber = "A1278AA"
      prisonApiMockServer.stubGetOffenderDetails(prisonNumber, 200)
      prisonApiMockServer.stubAddPersonalCareNeeds(1202691, LocalDate.of(2022, 1, 1))
      webTestClient
        .post()
        .uri("/body-scans/prisoners/$prisonNumber")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(validRequest)
        .exchange()
        .expectStatus().isNoContent
    }

    @Test
    fun `add body scan when prisoner not exist`() {
      val prisonNumber = "A1278AA"
      prisonApiMockServer.stubGetOffenderDetails(prisonNumber, 404)
      prisonApiMockServer.stubAddPersonalCareNeeds(1202691, LocalDate.of(2022, 1, 1))
      webTestClient
        .post()
        .uri("/body-scans/prisoners/$prisonNumber")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(validRequest)
        .exchange()
        .expectStatus().isNotFound
    }

    @Test
    fun `add body scan validation failure`() {
      val prisonNumber = "A1278AA"
      webTestClient
        .post()
        .uri("/body-scans/prisoners/$prisonNumber")
        .headers(
          setAuthorisation(
            roles = listOf("ROLE_MAINTAIN_HEALTH_PROBLEMS"),
            scopes = listOf("read", "write"),
          ),
        )
        .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .bodyValue(
          "{}",
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
          """.trimIndent(),
        )
    }
  }
}
