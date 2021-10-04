package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.regex.Pattern

class PrisonServiceTest {

  private val client = mock<PrisonApiClient>()

  private val service = PrisonService(client)

  private val prisonImage = "prisonImage".toByteArray()

  @Test
  fun `get moves`() {
    val moves = service.getMoves("NMI", LocalDate.now())
    assertThat(moves).allSatisfy { move -> assertThat(move.prisonNumber).matches(Pattern.compile("[A-Z]\\d{4}[A-Z][A-Z]")) }
  }

  @Test
  fun `gets prisoner image`() {
    whenever(client.getPrisonerImage(any())).thenReturn(prisonImage)

    val result = service.getPrisonerImage("A12345")

    assertThat(result).isEqualTo(prisonImage)
  }
}
