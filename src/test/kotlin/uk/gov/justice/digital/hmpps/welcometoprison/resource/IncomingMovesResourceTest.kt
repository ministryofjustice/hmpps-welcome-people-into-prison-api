package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.MoveType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.MovementService
import java.time.LocalDate

@Suppress("ClassName")
class IncomingMovesResourceTest : IntegrationTestBase() {
  @MockBean
  private lateinit var movementService: MovementService

  @Nested
  inner class `Find movements on day` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/incoming-moves/MDI?date=2020-01-02")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/incoming-moves/MDI?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `requires date param`() {
      webTestClient.get().uri("/incoming-moves/MDI")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Missing request value")
    }

    @Test
    fun `requires date param in correct format`() {
      webTestClient.get().uri("/incoming-moves/MDI?date=wibble")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isBadRequest
        .expectBody().jsonPath("userMessage").isEqualTo("Argument type mismatch")
    }

    @Test
    fun `returns json in expected format`() {
      val moves = listOf(
        Movement(
          firstName = "First",
          lastName = "Last",
          dateOfBirth = LocalDate.of(1980, 1, 2),
          prisonNumber = "A1234AA",
          pncNumber = "1234/1234567A",
          date = LocalDate.of(2021, 1, 2),
          moveType = MoveType.COURT_APPEARANCE,
          fromLocation = "Hull Court"
        ),
      )

      whenever(movementService.getMovements(any(), any())).thenReturn(
        moves
      )
      webTestClient.get().uri("/incoming-moves/MDI?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_INCOMING_MOVEMENTS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody().json("moves".loadJson())
    }

    @Test
    fun `calls service method with correct args`() {
      whenever(movementService.getMovements(any(), any())).thenReturn(
        listOf()
      )
      webTestClient.get().uri("/incoming-moves/MDI?date=2020-01-02")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_INCOMING_MOVEMENTS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      verify(movementService).getMovements("MDI", LocalDate.of(2020, 1, 2))
    }

    private fun String.loadJson(): String =
      IncomingMovesResourceTest::class.java.getResource("$this.json")?.readText()
        ?: throw AssertionError("file $this.json not found")
  }
}
