package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import java.time.LocalDate
import java.time.LocalTime

@Suppress("ClassName")
class RecentArrivalsResourceTest : IntegrationTestBase() {

  @Test
  fun `requires authentication`() {
    webTestClient.get().uri("/prisons/MDI/recent-arrivals?fromDate=2019-01-02&toDate=2020-01-02")
      .exchange()
      .expectStatus().isUnauthorized
  }

  @Test
  fun `requires correct role`() {
    webTestClient.get().uri("/prisons/MDI/recent-arrivals?fromDate=2019-01-02&toDate=2020-01-02")
      .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
      .exchange()
      .expectStatus().isForbidden
      .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
  }

  @Test
  fun `requires from date param`() {
    webTestClient.get().uri("/prisons/MDI/recent-arrivals?toDate=2020-01-02")
      .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("userMessage").isEqualTo("Missing request value")
  }

  @Test
  fun `requires toDate date param`() {
    webTestClient.get().uri("/prisons/MDI/recent-arrivals?fromDate=2020-01-02")
      .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("userMessage").isEqualTo("Missing request value")
  }

  @Test
  fun `requires fromDate param in correct format`() {
    webTestClient.get().uri("/prisons/MDI/recent-arrivals?fromDate=wibble&toDate=2020-01-02")
      .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("userMessage").isEqualTo("Argument type mismatch")
  }

  @Test
  fun `requires toDate param in correct format`() {
    webTestClient.get().uri("/prisons/MDI/recent-arrivals?toDate=wibble&fromDate=2020-01-02")
      .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("userMessage").isEqualTo("Argument type mismatch")
  }

  @Test
  fun `calls service method with correct args`() {
    prisonApiMockServer.stubGetMovementSuccess(
      "MDI",
      LocalDate.of(2019, 1, 2).atStartOfDay(),
      LocalDate.of(2020, 1, 2).atTime(LocalTime.MAX)
    )

    webTestClient.get().uri("/prisons/MDI/recent-arrivals?fromDate=2019-01-02&toDate=2020-01-02")
      .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """
        {"content":[
            {"prisonNumber":"G5155VP","dateOfBirth":"1966-04-05","firstName":"Gideon","lastName":"Herkimer","movementDateTime":"2021-07-15T07:08:00","location":"MDI-1-3-004"},
            {"prisonNumber":"A7925DY","dateOfBirth":"1997-05-06","firstName":"Prisonerhfirstname","lastName":"Prisonerhlastname","movementDateTime":"2021-08-04T14:15:27","location":"MDI-RECV"}
                    ],
         "pageable":{
            "sort":{"empty":true,"unsorted":true,"sorted":false},
            "offset":0,
            "pageNumber":0,
            "pageSize":50,
            "paged":true,
            "unpaged":false
                    },
         "totalPages":1,
         "totalElements":2,
         "last":true,
         "size":50,
         "number":0,
         "sort":{"empty":true,"unsorted":true,"sorted":false},
         "numberOfElements":2,
         "first":true,
         "empty":false
        }
        """.trimIndent()
      )
  }
  @Test
  fun `calls service method with correct args response do not have location`() {
    prisonApiMockServer.stubGetMovementSuccessWithNoLocation(
      "MDI",
      LocalDate.of(2019, 1, 2).atStartOfDay(),
      LocalDate.of(2020, 1, 2).atTime(LocalTime.MAX)
    )

    webTestClient.get().uri("/prisons/MDI/recent-arrivals?fromDate=2019-01-02&toDate=2020-01-02")
      .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .json(
        """
        {"content":[
            {"prisonNumber":"G5155VP","dateOfBirth":"1966-04-05","firstName":"Gideon","lastName":"Herkimer","movementDateTime":"2021-07-15T07:08:00","location":"MDI-1-3-004"},
            {"prisonNumber":"A7925DY","dateOfBirth":"1997-05-06","firstName":"Prisonerhfirstname","lastName":"Prisonerhlastname","movementDateTime":"2021-08-04T14:15:27"}
                    ],
         "pageable":{
            "sort":{"empty":true,"unsorted":true,"sorted":false},
            "offset":0,
            "pageNumber":0,
            "pageSize":50,
            "paged":true,
            "unpaged":false
                    },
         "totalPages":1,
         "totalElements":2,
         "last":true,
         "size":50,
         "number":0,
         "sort":{"empty":true,"unsorted":true,"sorted":false},
         "numberOfElements":2,
         "first":true,
         "empty":false
        }
        """.trimIndent()
      )
  }
}
