package uk.gov.justice.digital.hmpps.welcometoprison.formatter

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LocationFormatterTest {

  var locationFormatter: LocationFormatter = LocationFormatter()

  @Test
  fun `When value is RECP formatter should return 'Reception'`() {
    Assertions.assertEquals(locationFormatter.format("RECP"), "Reception")
  }

  @Test
  fun `When value is null formatter should return empty string`() {
    Assertions.assertEquals(locationFormatter.format(null), "")
  }

  @Test
  fun `When value is other formatter should return the same value`() {
    val expectedValue = "D-3-017"
    Assertions.assertEquals(locationFormatter.format(expectedValue), expectedValue)
  }
}
