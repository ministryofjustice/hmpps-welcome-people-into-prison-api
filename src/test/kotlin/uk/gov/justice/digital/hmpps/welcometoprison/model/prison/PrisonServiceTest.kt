package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class PrisonServiceTest {

  private val client = mock<PrisonApiClient>()

  private val service = PrisonService(client)

  private val prisonImage = "prisonImage".toByteArray()

  @Test
  fun `gets prisoner image`() {
    whenever(client.getPrisonerImage(any())).thenReturn(prisonImage)

    val result = service.getPrisonerImage("A12345")

    assertThat(result).isEqualTo(prisonImage)
  }
}
