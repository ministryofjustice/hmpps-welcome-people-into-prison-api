package uk.gov.justice.digital.hmpps.welcometoprison.resource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase

private const val REQUESTING_USER = "request-user"

class DprReportingResourceTest : IntegrationTestBase() {
  @Value("\${dpr.lib.system.role}")
  lateinit var systemRole: String

  @BeforeEach
  fun setUp() {
    manageUsersApiMockServer.stubLookupUsersRoles(REQUESTING_USER, listOf("VIEW_ARRIVALS"))
    manageUsersApiMockServer.stubLookupUserCaseload(REQUESTING_USER, "LEI", listOf("MDI"))
  }

  @DisplayName("GET /definitions")
  @Nested
  inner class GetDefinitions {
    private val url = "/definitions"

    @DisplayName("works")
    @Nested
    inner class HappyPath {

      @Test
      fun `returns the definitions of all the reports`() {
        webTestClient.get().uri(url)
          .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read")))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isOk
          .expectBody().jsonPath("$.length()").isEqualTo(1)
          .jsonPath("$[0].authorised").isEqualTo("true")
      }

      @Test
      fun `returns the definitions of all the reports but not authorises as no user in context`() {
        webTestClient.get().uri(url)
          .headers(setAuthorisation(user = null, roles = listOf(systemRole), scopes = listOf("read")))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isOk
          .expectBody().jsonPath("$.length()").isEqualTo(1)
          .jsonPath("$[0].authorised").isEqualTo("false")
      }

      @Test
      fun `returns the not auth definitions of the reports`() {
        manageUsersApiMockServer.stubLookupUsersRoles(REQUESTING_USER, listOf("ANOTHER_USER_ROLE"))

        webTestClient.get().uri(url)
          .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read")))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.length()").isEqualTo(1)
          .jsonPath("$[0].authorised").isEqualTo("false")
      }
    }
  }
}
