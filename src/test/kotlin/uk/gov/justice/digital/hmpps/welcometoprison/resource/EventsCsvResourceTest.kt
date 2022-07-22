package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Bindable.listOf
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase

@Suppress("ClassName")
class EventsCsvResourceTest : IntegrationTestBase() {

  @Nested
  inner class `Get events in CSV` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/events?start-date=2020-01-02")
        .accept(MediaType.parseMediaType("text/csv"))
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/events?start-date=2020-01-02")
        .accept(MediaType.parseMediaType("text/csv"))
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
    }

    @Test
    fun `requires date param`() {
      webTestClient.get().uri("/events")
        .accept(MediaType.parseMediaType("text/csv"))
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    fun `requires date param in correct format`() {
      webTestClient.get().uri("/events?start-date=wibble")
        .accept(MediaType.parseMediaType("text/csv"))
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isBadRequest
    }

    @Test
    @Sql("classpath:repository/confirmed-arrival.sql")
    fun `calls service method with correct args`() {

      webTestClient.get().uri("/events?start-date=2020-01-06")
        .accept(MediaType.parseMediaType("text/csv"))
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectHeader().valueMatches("Content-Type", "text/csv;charset=UTF-8")
        .expectBody().consumeWith {
          val csv = String(it.responseBody)
          assertThat(csv).isEqualTo(
            "id,timestamp,arrivalDate,prisonId,arrivalType,username\n" +
              "7,2020-01-06T01:01:01,2020-01-06,MIK,NEW_TO_PRISON,\"User U\"\n" +
              "8,2020-01-07T01:01:01,2020-01-07,MIK,\"NEW_BOOKING_EXISTING_OFFENDER\",\"Mr X\"\n"
          )
        }
    }
  }
}
