package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferIn
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferInDetail
import java.time.LocalDateTime

@Suppress("ClassName")
class TransferResourceTest : IntegrationTestBase() {

  @MockBean
  private lateinit var prisonApiClient: PrisonApiClient

  companion object {
    val transferInDetail = TransferInDetail(
      "MDI-RECP",
      "some comment",
      LocalDateTime.of(2021, 11, 15, 1, 0, 0)
    )
  }

  @Nested
  inner class `Transfer in` {
    @Test
    fun `Requires authentication`() {
      webTestClient
        .post()
        .uri("/transfer-in/A1234BC")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Requires correct role`() {

      webTestClient
        .post()
        .uri("/transfer-in/A1234BC")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("write")))
        .bodyValue(transferInDetail)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `Should call prisonApi successfully`() {

      val transferInDetails = TransferIn(
        "MDI-RECP",
        "some comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      webTestClient
        .post()
        .uri("/transfer-in/A1234BC")
        .headers(setAuthorisation(roles = listOf("ROLE_TRANSFER_PRISONER"), scopes = listOf("write")))
        .bodyValue(transferInDetail)
        .exchange()
        .expectStatus().isOk

      verify(prisonApiClient).transferIn("A1234BC", transferInDetails)
    }
  }
}
