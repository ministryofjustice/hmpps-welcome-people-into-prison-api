package uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MatchPrisonerResponseTest {
  @Test
  fun `given null status then prisoner is not current`() {
    assertThat(reference.currentPrisoner).isFalse
  }

  @Test
  fun `given INACTIVE OUT status then prisoner is not current`() {
    assertThat(reference.copy(status = "INACTIVE OUT").currentPrisoner).isFalse
  }

  @Test
  fun `given status is present but not INACTIVE OUT then prisoner is current`() {
    assertThat(reference.copy(status = "XXXXX").currentPrisoner).isTrue
  }

  companion object {
    val reference = MatchPrisonerResponse("A123AA", "123/456", null)
  }
}
