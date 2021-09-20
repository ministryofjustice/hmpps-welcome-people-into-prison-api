package uk.gov.justice.digital.hmpps.welcometoprison.resource

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.MoveType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.utils.LoadJsonHelper.Companion.loadJson
import java.time.LocalDate

@Suppress("ClassName")
class IncomingMovesResourceTest : IntegrationTestBase() {
	@MockBean
	private lateinit var prisonService: PrisonService

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

			whenever(prisonService.getMoves("MDI", LocalDate.of(2020, 1, 2))).thenReturn(
				listOf(
					Movement(
						firstName = "First",
						lastName = "Last",
						dateOfBirth = LocalDate.of(1980, 2, 23),
						prisonNumber = "A1278AA",
						pncNumber = "1234/1234589A",
						date = LocalDate.of(2021, 1, 2),
						moveType = MoveType.PRISON_TRANSFER,
						fromLocation = "MDI"
					)
				)
			)

			webTestClient.get().uri("/incoming-moves/MDI?date=2020-01-02")
				.headers(setAuthorisation(roles = listOf("ROLE_VIEW_INCOMING_MOVEMENTS"), scopes = listOf("read")))
				.exchange()
				.expectStatus().isOk
				.expectBody().json("moves".loadJson(this))
		}

		@Test
		fun `calls service method with correct args`() {
			webTestClient.get().uri("/incoming-moves/MDI?date=2020-01-02")
				.headers(setAuthorisation(roles = listOf("ROLE_VIEW_INCOMING_MOVEMENTS"), scopes = listOf("read")))
				.exchange()
				.expectStatus().isOk

			basmApiMockServer.verify(
				getRequestedFor(
					urlEqualTo(
						"/api/moves?include=profile.person,from_location,to_location&filter%5Bto_location_id%5D=a2bc2abf-75fe-4b7f-bf5a-a755bc290757&filter%5Bdate_from%5D=2020-01-02&filter%5Bdate_to%5D=2020-01-02&filter%5Bstatus%5D=requested,accepted,booked,in_transit,completed&page=1&per_page=200&sort%5Bby%5D=date&sort%5Bdirection%5D=asc"
					)
				)
			)
		}
	}
}
