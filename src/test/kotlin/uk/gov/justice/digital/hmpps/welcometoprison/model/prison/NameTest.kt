package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name.properCase

class NameTest {
  @ParameterizedTest
  @CsvSource(
    "'',''",
    "A,A",
    "AB,Ab",
    "ABCDEF,Abcdef",
    "SARAH-LOUISE,Sarah-Louise",
    "D'ARRAS,D'Arras",
    "Tom Jim Baker,Tom Jim Baker",
    "tom Jim Baker,Tom Jim Baker",
    "Tom jim baker,Tom Jim Baker",
    "'Tom, Jim Baker', 'Tom, Jim Baker'",
    "'Tom, jim, Baker','Tom, Jim, Baker'",
    "'tom, jim baker', 'Tom, Jim Baker'",
    "'tom, Jim, baker','Tom, Jim, Baker'",
    "'jim,roberts', 'Jim,Roberts'",
  )
  fun `scenarios`(nomisName: String, expectedResult: String) {
    assertThat(properCase(nomisName)).isEqualTo(expectedResult)
  }
}
