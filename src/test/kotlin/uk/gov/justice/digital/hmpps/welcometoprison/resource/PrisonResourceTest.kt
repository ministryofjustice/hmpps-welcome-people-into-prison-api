package uk.gov.justice.digital.hmpps.welcometoprison.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.welcometoprison.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Prison
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerDetails
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.UserCaseLoad
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import java.time.LocalDate

@Suppress("ClassName")
class PrisonResourceTest : IntegrationTestBase() {
  @MockBean
  private lateinit var prisonService: PrisonService

  @MockBean
  private lateinit var prisonerSearchService: PrisonerSearchService

  @Nested
  inner class `Get Prisoner image` {
    @Test
    fun `requires authentication`() {
      webTestClient.get().uri("/prisoners/A12345/image")
        .exchange()
        .expectStatus().isUnauthorized
    }

    @Test
    fun `requires correct role`() {
      webTestClient.get().uri("/prisoners/A12345/image")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `returns image in expected format`() {
      val image: ByteArray = "imageString".toByteArray()

      whenever(prisonService.getPrisonerImage(any())).thenReturn(
        "imageString".toByteArray()
      )
      val response = webTestClient.get().uri("/prisoners/A12345/image")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody(ByteArray::class.java)
        .returnResult().responseBody

      assertThat(response).isEqualTo(image)
    }

    @Test
    fun `calls service method with correct args`() {
      webTestClient.get().uri("/prisoners/A12345/image")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk

      verify(prisonService).getPrisonerImage("A12345")
    }
  }

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

      whenever(prisonService.getPrison(any())).thenReturn(Prison(prisonName = "Nottingham (HMP)"))

      webTestClient.get()
        .uri("/prison/NMI")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("description").isEqualTo("Nottingham (HMP)")

      verify(prisonService).getPrison("NMI")
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

      whenever(prisonService.getUserCaseLoads()).thenReturn(
        listOf(
          UserCaseLoad(
            caseLoadId = "MDI",
            description = "Moorland Closed (HMP & YOI)"
          )
        )
      )

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
      webTestClient.get().uri("/prisoners/A1234BC")
        .headers(setAuthorisation(roles = listOf(), scopes = listOf("read")))
        .exchange()
        .expectStatus().isForbidden
        .expectBody().jsonPath("userMessage").isEqualTo("Access denied")
    }

    @Test
    fun `User has correct role`() {
      webTestClient.get().uri("/prisoners/A1234BC")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
    }

    @Test
    fun `Returns prisoner details`() {

      whenever(prisonerSearchService.getPrisoner(any())).thenReturn(
        PrisonerDetails(
          firstName = "Jim",
          lastName = "Smith",
          dateOfBirth = LocalDate.of(1970, 12, 25),
          prisonNumber = "A1234BC",
          pncNumber = "11/1234",
          croNumber = "12/4321",
          isCurrentPrisoner = false,
          sex = "Male"
        )
      )

      webTestClient.get()
        .uri("/prisoners/A1234BC")
        .headers(setAuthorisation(roles = listOf("ROLE_VIEW_ARRIVALS"), scopes = listOf("read")))
        .exchange()
        .expectStatus().isOk
        .expectBody()
        .jsonPath("firstName").isEqualTo("Jim")
        .jsonPath("lastName").isEqualTo("Smith")
        .jsonPath("dateOfBirth").isEqualTo("1970-12-25")
        .jsonPath("prisonNumber").isEqualTo("A1234BC")
        .jsonPath("pncNumber").isEqualTo("11/1234")
        .jsonPath("croNumber").isEqualTo("12/4321")

      verify(prisonerSearchService).getPrisoner("A1234BC")
    }
  }
}
