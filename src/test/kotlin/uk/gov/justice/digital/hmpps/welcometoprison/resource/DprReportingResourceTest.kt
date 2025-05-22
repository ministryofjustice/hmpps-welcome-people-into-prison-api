package uk.gov.justice.digital.hmpps.welcometoprison.resource
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalRepository

private const val REQUESTING_USER = "request-user"
private const val CLIENT_ID = "hmpps-welcome-people-into-prison-api"

class DprReportingResourceTest : IntegrationTestBase() {
  @Value("\${dpr.lib.system.role}")
  lateinit var systemRole: String

  @Autowired
  lateinit var repository: ConfirmedArrivalRepository

  @BeforeEach
  fun setUp() {
    manageUsersApiMockServer.stubLookupUsersRoles(REQUESTING_USER, listOf("MANAGE_RES_LOCATIONS_OP_CAP"))
    manageUsersApiMockServer.stubLookupUserCaseload(REQUESTING_USER, "LEI", listOf("MDI"))
  }

  @AfterEach
  fun afterEach() {
    repository.deleteAll()
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
          .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read"), clientId = CLIENT_ID))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isOk
          .expectBody().jsonPath("$.length()").isEqualTo(1)
          .jsonPath("$[0].authorised").isEqualTo("true")
      }

      @Test
      fun `returns the definitions of all the reports but not authorises as no user in context`() {
        webTestClient.get().uri(url)
          .headers(setAuthorisation(user = null, roles = listOf(systemRole), scopes = listOf("read"), clientId = CLIENT_ID))
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
          .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read"), clientId = CLIENT_ID))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.length()").isEqualTo(1)
          .jsonPath("$[0].authorised").isEqualTo("false")
      }
    }
  }

  @DisplayName("GET /definitions/transactions/arrivals")
  @Nested
  inner class GetDefinitionDetails {
    private val url = "/definitions/transactions/arrivals"

    @Test
    fun `report definition denied when user has incorrect role`() {
      manageUsersApiMockServer.stubLookupUsersRoles(REQUESTING_USER, listOf("ANOTHER_USER_ROLE"))

      webTestClient.get().uri(url)
        .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read"), clientId = CLIENT_ID))
        .header("Content-Type", "application/json")
        .exchange()
        .expectStatus().isForbidden
    }

    @DisplayName("works")
    @Nested
    inner class HappyPath {

      @Test
      fun `returns the definition of the report`() {
        webTestClient.get().uri(url)
          .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read"), clientId = CLIENT_ID))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .json(
            // language=json
            """
           {
              "id": "transactions",
              "name": "Transactions for WPIP",
              "description": "List of confirmed arrivals in WPIP",
              "variant": {
                "id": "arrivals",
                "name": "Transactions for WPIP",
                "resourceName": "reports/transactions/arrivals",
                "description": "Details each confirmed arrival that has occurred in a prison",
                "printable": true
              }
            }
            """,
          )
      }
    }
  }

  @DisplayName("GET /reports")
  @Nested
  inner class GetReports {
    @DisplayName("GET /reports/transactions/arrivals")
    @Nested
    inner class RunTransactionReport {
      private val url = "/reports/transactions/arrivals"

      @Test
      fun `returns 403 when user does not have the role`() {
        manageUsersApiMockServer.stubLookupUsersRoles(REQUESTING_USER, listOf("ANOTHER_USER_ROLE"))

        webTestClient.get().uri(url)
          .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read"), clientId = CLIENT_ID))
          .header("Content-Type", "application/json")
          .exchange()
          .expectStatus().isForbidden
      }

      @DisplayName("works")
      @Nested
      inner class HappyPath {

        @Test
        @Sql("classpath:repository/confirmed-arrival.sql")
        fun `returns a page of the report`() {
          webTestClient.get().uri(url)
            .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read"), clientId = CLIENT_ID))
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk
            .expectBody().json(
              // language=json
              """
            [
              {
                "prison_id": "MDI",
                "arrival_time": "01/01/2020 01:01",
                "arrival_type": "NEW_TO_PRISON",
                "username": "xyz"
              },
              {
                "prison_id": "MDI",
                "arrival_time": "02/01/2020 01:01",
                "arrival_type": "NEW_TO_PRISON",
                "username": "zzzz"
              }
            ]
          """,
              JsonCompareMode.LENIENT,
            )
        }

        @Test
        @Sql("classpath:repository/confirmed-arrival.sql")
        fun `returns no data when user does not have the caseload`() {
          manageUsersApiMockServer.stubLookupUserCaseload(REQUESTING_USER, "BXI", listOf("BXI"))

          webTestClient.get().uri(url)
            .headers(setAuthorisation(user = REQUESTING_USER, roles = listOf(systemRole), scopes = listOf("read"), clientId = CLIENT_ID))
            .header("Content-Type", "application/json")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(0)
        }
      }
    }
  }
}
