package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences.ConfirmTemporaryAbsenceRequest
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson
import java.time.LocalDateTime

@Suppress("ClassName")
class TemporaryAbsencesResourceTest : IntegrationTestBase() {

  @Nested
  inner class `Get Temporary absences` {

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns json in expected format`() {
      prisonApiMockServer.stubGetTemporaryAbsencesWithTwoRecords("MDI")
      webTestClient.get().uri("/prison/MDI/temporary-absences")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("temporaryAbsences".loadJson(this))
    }
  }

  @Nested
  inner class `Get Temporary absence` {

    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences/A1234AA")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prison/MDI/temporary-absences/A1234AA")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    @Deprecated("endpoint to be removed following update in frontend")
    fun `returns json in expected format`() {
      prisonerSearchMockServer.stubGetPrisoner(200)
      prisonApiMockServer.stubGetTemporaryAbsencesWithTwoRecords("MDI")
      webTestClient.get().uri("/prison/MDI/temporary-absences/A1234AA")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("temporaryAbsence".loadJson(this))
    }

    @Test
    fun `when calling endpoint without prisonId returns json in expected format`() {
      prisonerSearchMockServer.stubGetPrisoner(200)
      prisonApiMockServer.stubGetTemporaryAbsencesWithTwoRecords("MDI")
      webTestClient.get().uri("/temporary-absences/A1234AA")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("temporaryAbsence".loadJson(this))
    }
  }

  @Nested
  inner class `Confirm arrival from temporary absence` {

    @Test
    fun `confirm arrival`() {
      prisonApiMockServer.stubConfirmTemporaryAbsencesSuccess("G5666UK")
      val token = getAuthorisation(roles = listOf("ROLE_TRANSFER_PRISONER"), scopes = listOf("write"))
      val confirmTemporaryAbsenceRequest = ConfirmTemporaryAbsenceRequest(
        "NMI",
        "ET",
        "Comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )
      webTestClient
        .post()
        .uri("/temporary-absences/G5666UK/confirm")
        .bodyValue(confirmTemporaryAbsenceRequest)
        .withBearerToken(token)
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("prisonNumber").isEqualTo("G5666UK")
        .jsonPath("location").isEqualTo("Reception")
    }

    @Test
    fun `requires authentication`() {
      val confirmTemporaryAbsenceRequest = ConfirmTemporaryAbsenceRequest(
        "NMI",
        "ET",
        "Comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      webTestClient.post().uri("/temporary-absences/G5666UK/confirm")
        .bodyValue(confirmTemporaryAbsenceRequest)
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      val token = getAuthorisation(roles = listOf(), scopes = listOf("write"))
      val confirmTemporaryAbsenceRequest = ConfirmTemporaryAbsenceRequest(
        "NMI",
        "ET",
        "Comment",
        LocalDateTime.of(2021, 11, 15, 1, 0, 0)
      )

      webTestClient.post().uri("/temporary-absences/G5666UK/confirm")
        .bodyValue(confirmTemporaryAbsenceRequest)
        .withBearerToken(token)
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }
  }
}
