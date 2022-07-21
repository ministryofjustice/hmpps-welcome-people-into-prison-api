package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Bindable.listOf
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase

@Suppress("ClassName")
class EventsCsvResourceTest : IntegrationTestBase() {

  @Nested
  inner class `Get events in CSV` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/events")
        .accept(MediaType.parseMediaType("text/csv"))
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/events")
        .accept(MediaType.parseMediaType("text/csv"))
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `requires date param`() {
      webTestClient.get().uri("/events")
        .accept(MediaType.parseMediaType("text/csv"))
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Missing request value")
    }

    @Test
    fun `requires date param in correct format`() {
      webTestClient.get().uri("/events?start-date=wibble")
        .accept(MediaType.parseMediaType("text/csv"))
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Argument type mismatch")
    }

    @Test
    fun `calls service method with correct args`() {

      webTestClient.get().uri("/events?start-date=2020-01-02")
        .accept(MediaType.parseMediaType("text/csv"))
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
    }
  }
}
