package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase

@Suppress("ClassName")
class PrisonResourceTest : IntegrationTestBase() {

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
      prisonRegisterMockServer.stubGetPrison("NMI")

      webTestClient.get()
        .uri("/prison/NMI")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("description").isEqualTo("Nottingham (HMP)")
    }
  }

  @Nested
  inner class `Get user case loads` {
    @Test
    fun `Requires authentication`() {
      webTestClient.get().uri("/prison/users/me/caseLoads")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Returns case loads for a user`() {
      prisonApiMockServer.stubGetUserCaseLoads()

      webTestClient.get()
        .uri("/prison/users/me/caseLoads")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("$[0].caseLoadId").isEqualTo("MDI")
        .jsonPath("$[0].description").isEqualTo("Moorland Closed (HMP & YOI)")
    }
  }

  @Nested
  inner class `Get Prisoner` {
    @Test
    fun `Requires authentication`() {
      webTestClient.get().uri("/prisoners/A1234BC")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `Requires correct role`() {
      prisonerSearchMockServer.stubGetPrisoner(200)
      webTestClient.get().uri("/prisoners/A1234BC")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `User has correct role`() {
      prisonerSearchMockServer.stubGetPrisoner(200)
      webTestClient.get().uri("/prisoners/A1234BC")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Returns prisoner details`() {
      prisonerSearchMockServer.stubGetPrisoner(200)

      webTestClient.get()
        .uri("/prisoners/A1278AA")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("firstName").isEqualTo("Jim")
        .jsonPath("lastName").isEqualTo("Smith")
        .jsonPath("dateOfBirth").isEqualTo("1991-07-31")
        .jsonPath("prisonNumber").isEqualTo("A1278AA")
        .jsonPath("pncNumber").isEqualTo("1234/1234589A")
        .jsonPath("croNumber").isEqualTo("SF80/655108T")
    }
  }
}
