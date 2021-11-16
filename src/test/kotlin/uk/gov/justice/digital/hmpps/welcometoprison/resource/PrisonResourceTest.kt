package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Prison
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferInDetail
import java.time.LocalDateTime

@Suppress("ClassName")
class PrisonResourceTest : IntegrationTestBase() {
  @MockBean
  private lateinit var prisonService: PrisonService

  @Nested
  inner class `Get Prisoner image` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prison/prisoner/A12345/image")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prison/prisoner/A12345/image")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns image in expected format`() {
      val image: ByteArray = "imageString".toByteArray()

      whenever(prisonService.getPrisonerImage(any())).thenReturn(
        "imageString".toByteArray()
      )
      val response = webTestClient.get().uri("/prison/prisoner/A12345/image")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody(ByteArray::class.java)
        .returnResult().responseBody

      assertThat(response).isEqualTo(image)
    }

    @Test
    fun `calls service method with correct args`() {
      webTestClient.get().uri("/prison/prisoner/A12345/image")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      verify(prisonService).getPrisonerImage("A12345")
    }
  }

  @Nested
  inner class `Get Prison` {
    @Test
    fun `Requires authentication`() {
      webTestClient.get().uri("/prison/NMI")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Requires correct role`() {
      webTestClient.get().uri("/prison/NMI")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `Returns prison name as description`() {

      whenever(prisonService.getPrison(any())).thenReturn(Prison(prisonName = "Nottingham (HMP)"))

      webTestClient.get()
        .uri("/prison/NMI")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("description").isEqualTo("Nottingham (HMP)")

      verify(prisonService).getPrison("NMI")
    }
  }

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
        .uri("prison/prisoner/A1234BC/transfer-in")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Requires correct role`() {

      webTestClient
        .post()
        .uri("prison/prisoner/A1234BC/transfer-in")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("write")))
        .bodyValue(transferInDetail)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `Should call prison service with correct args`() {

      val transferInDetails = TransferInDetail(
        "MDI-RECP",
        "some comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      webTestClient
        .post()
        .uri("prison/prisoner/A1234BC/transfer-in")
        .headers(setAuthorisation(roles = listOf("ROLE_TRANSFER_PRISONER"), scopes = listOf("write")))
        .bodyValue(transferInDetail)
        .exchange()
        .expectStatus().isOk

      verify(prisonService).transferInOffender("A1234BC", transferInDetails)
    }
  }
}
