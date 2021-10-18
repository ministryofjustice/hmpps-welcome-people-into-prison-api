package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AgeTest {
  @Test
  fun `18 tomorrow`() {
    assertThat(
      Age.lessThanEighteenYears(
        LocalDate.of(1961, 5, 29),
        LocalDate.of(1979, 5, 28)
      )
    ).isTrue
  }

  @Test
  fun `18 today`() {
    assertThat(
      Age.lessThanEighteenYears(
        LocalDate.of(1961, 5, 29),
        LocalDate.of(1979, 5, 29)
      )
    ).isFalse
  }
}
