package uk.gov.justice.digital.hmpps.welcometoprison.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.exception.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerImage
import java.time.LocalDate
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService

class PrisonServiceTest {

  private val client = mock<PrisonApiClient>()

  private val service = PrisonService(client)

  private val prisonImage = "prisonImage".toByteArray()

  private val faceFront2001 = PrisonerImage(
    LocalDate.of(2001, 1, 1),
    1,
    "FRONT",
    "OFF_BKG",
    "FACE",
    1
  )

  private val faceBack2002 = PrisonerImage(
    LocalDate.of(2002, 1, 1),
    2,
    "BACK",
    "OFF_BKG",
    "FACE",
    2
  )

  private val armFront2003 = PrisonerImage(
    LocalDate.of(2003, 1, 1),
    3,
    "FRONT",
    "OFF_BKG",
    "ARM",
    3
  )

  private val faceFront2004 = PrisonerImage(
    LocalDate.of(2001, 1, 1),
    1,
    "FRONT",
    "OFF_BKG",
    "FACE",
    1
  )

  @Test
  fun `gets prisoner image`() {
    val prisonImages = listOf(faceFront2001)
    whenever(client.getPrisonerImages(any())).thenReturn(prisonImages)
    whenever(client.getPrisonerImage(any())).thenReturn(prisonImage)

    val result = service.getPrisonerImage("A12345")

    assertThat(result).isEqualTo(prisonImage)
    verify(client).getPrisonerImages("A12345")
    verify(client).getPrisonerImage(faceFront2001.imageId)
  }

  @Test
  fun `gets latest front-facing image`() {
    val prisonImages = listOf(faceFront2001, faceBack2002, armFront2003, faceFront2004)
    whenever(client.getPrisonerImages(any())).thenReturn(prisonImages)
    whenever(client.getPrisonerImage(any())).thenReturn(prisonImage)

    val result = service.getPrisonerImage("A12345")

    assertThat(result).isEqualTo(prisonImage)
    verify(client).getPrisonerImages("A12345")
    verify(client).getPrisonerImage(faceFront2004.imageId)
  }

  @Test
  fun `filters non front-facing images`() {
    val prisonImages = listOf(faceFront2001, faceBack2002, armFront2003)
    whenever(client.getPrisonerImages(any())).thenReturn(prisonImages)
    whenever(client.getPrisonerImage(any())).thenReturn(prisonImage)

    val result = service.getPrisonerImage("A12345")

    assertThat(result).isEqualTo(prisonImage)
    verify(client).getPrisonerImages("A12345")
    verify(client).getPrisonerImage(faceFront2001.imageId)
  }

  @Test
  fun `given no front-facing photo throws NotFoundException`() {
    val prisonImages = listOf(faceBack2002, armFront2003)
    whenever(client.getPrisonerImages(any())).thenReturn(prisonImages)

    assertThatThrownBy { service.getPrisonerImage("A12345") }
      .isExactlyInstanceOf(NotFoundException::class.java)
      .hasMessage("No front-facing image of the offenders face found for offender number: A12345")

    verify(client).getPrisonerImages("A12345")
    verifyNoMoreInteractions(client)
  }
}
