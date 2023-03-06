package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AgeTest {
  @Test
  fun `21 tomorrow`() {
    assertThat(
      Age.lessThanTwentyOneYears(
        LocalDate.of(1961, 5, 29),
        LocalDate.of(1982, 5, 28),
      ),
    ).isTrue
  }

  @Test
  fun `21 today`() {
    assertThat(
      Age.lessThanTwentyOneYears(
        LocalDate.of(1961, 5, 29),
        LocalDate.of(1982, 5, 29),
      ),
    ).isFalse
  }
}
