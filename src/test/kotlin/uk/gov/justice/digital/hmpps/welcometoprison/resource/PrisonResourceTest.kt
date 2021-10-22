package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService

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
    fun `returns json in expected format`() {
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
    fun `returns json in expected format`() {
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
}
