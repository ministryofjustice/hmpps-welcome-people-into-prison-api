package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase

@Suppress("ClassName")
class ImprisonmentReasonsResourceTest : IntegrationTestBase() {
  @Nested
  inner class `Get imprisonment statuses` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/imprisonment-statuses")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/imprisonment-statuses")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns the correct number of ImprisonmentStatus objects`() {
      webTestClient.get().uri("/imprisonment-statuses")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$.length()").isEqualTo(13)
    }

    @Test
    fun `returns ImprisonmentStatus with single movement reason`() {
      webTestClient.get().uri("/imprisonment-statuses")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectBody()
        .jsonPath("$[0].description").isEqualTo("On remand")
        .jsonPath("$[0].imprisonmentStatusCode").isEqualTo("RX")
        .jsonPath("$[0].movementReasons[0].movementReasonCode").isEqualTo("R")
    }

    @Test
    fun `returns ImprisonmentStatus with multiple movement reasons`() {
      webTestClient.get().uri("/imprisonment-statuses")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectBody()
        .jsonPath("$[2].description").isEqualTo("Determinate sentence")
        .jsonPath("$[2].imprisonmentStatusCode").isEqualTo("SENT")
        .jsonPath("$[2].secondLevelTitle").isEqualTo("What is the type of determinate sentence?")
        .jsonPath("$[2].movementReasons[0].description").isEqualTo("Extended sentence for public protection")
        .jsonPath("$[2].movementReasons[0].movementReasonCode").isEqualTo("26")
        .jsonPath("$[2].movementReasons[1].description").isEqualTo("Imprisonment without option of a fine")
        .jsonPath("$[2].movementReasons[1].movementReasonCode").isEqualTo("I")
        .jsonPath("$[2].movementReasons.length()").isEqualTo(4)
    }
  }
}
