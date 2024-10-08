package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class MatchPrisonerResponseTest {
  @Test
  fun `given INACTIVE OUT status then prisoner is not current`() {
    assertThat(reference.copy(status = "INACTIVE OUT").isCurrentPrisoner).isFalse
  }

  @Test
  fun `given status is present but not INACTIVE OUT then prisoner is current`() {
    assertThat(reference.copy(status = "XXXXX").isCurrentPrisoner).isTrue
  }

  @Test
  fun `isCurrentPrisoner should return false when status is null`() {
    val prisonerResponseWithNullStatus = reference.copy(status = null)

    assertThat(prisonerResponseWithNullStatus.isCurrentPrisoner).isFalse
  }

  @Test
  fun `isCurrentPrisoner should return false when status is INACTIVE OUT`() {
    val prisonerResponseWithInactiveOutStatus = reference.copy(status = INACTIVE_OUT)

    assertThat(prisonerResponseWithInactiveOutStatus.isCurrentPrisoner).isFalse
  }

  companion object {
    val reference =
      MatchPrisonerResponse("JIM", "SMITH", LocalDate.of(1961, 4, 1), "A123AA", "123/456", "SF80/655108T", "Male", "ACTIVE IN", lastMovementTypeCode = "ADM", prisonId = "MDI")
  }
}
