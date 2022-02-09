package uk.gov.justice.digital.hmpps.welcometoprison.formatter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LocationFormatterTest {

  var locationFormatter: LocationFormatter = LocationFormatter()

  @Test
  fun `When value is RECP formatter should return 'Reception'`() {
    assertThat(locationFormatter.format("RECP")).isEqualTo("Reception")
  }

  @Test
  fun `When value is other formatter should return the same value`() {
    val expectedValue = "D-3-017"
    assertThat(locationFormatter.format(expectedValue)).isEqualTo(expectedValue)
  }
}
