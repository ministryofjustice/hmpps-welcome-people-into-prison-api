package uk.gov.justice.digital.hmpps.bodyscan.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanPrisonApiClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareCounter
import uk.gov.justice.digital.hmpps.bodyscan.model.LimitStatusResponse
import uk.gov.justice.digital.hmpps.bodyscan.model.LimitStatusResponse.BodyScanStatus.CLOSE_TO_LIMIT
import uk.gov.justice.digital.hmpps.bodyscan.model.LimitStatusResponse.BodyScanStatus.DO_NOT_SCAN
import uk.gov.justice.digital.hmpps.bodyscan.model.LimitStatusResponse.BodyScanStatus.OK_TO_SCAN
import java.time.LocalDate
import java.time.Year

class LimitStatusServiceTest {
  private val bodyScanPrisonApiClient: BodyScanPrisonApiClient = mock()
  private val limitStatusService = LimitStatusService(bodyScanPrisonApiClient)

  @Test
  fun `get limit status for year and prison numbers`() {
    val prisonNumbers = listOf("G8266VA", "G8874VB", "G8874VC", "G8874VD", "G8874VE")
    val year = Year.of(2022)
    whenever(bodyScanPrisonApiClient.getPersonalCareNeedsForPrisonNumbers(any(), any(), any(), any())).thenReturn(
      listOf(
        PersonalCareCounter("G8266VA", 99), // OK_TO_SCAN
        PersonalCareCounter("G8874VB", 100), // CLOSE_TO_LIMIT
        PersonalCareCounter("G8874VC", 115), // CLOSE_TO_LIMIT
        PersonalCareCounter("G8874VD", 116), // DO_NOT_SCAN
        PersonalCareCounter("G8874VE", 117) // DO_NOT_SCAN
      )
    )
    val result = limitStatusService.getLimitStatusForYearAndPrisonNumbers(year, prisonNumbers)
    verify(bodyScanPrisonApiClient).getPersonalCareNeedsForPrisonNumbers(
      "BSCAN", LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31), prisonNumbers
    )

    assertThat(result.getStatusFor("G8266VA")).isEqualTo(OK_TO_SCAN)
    assertThat(result.getStatusFor("G8874VB")).isEqualTo(CLOSE_TO_LIMIT)
    assertThat(result.getStatusFor("G8874VC")).isEqualTo(CLOSE_TO_LIMIT)
    assertThat(result.getStatusFor("G8874VD")).isEqualTo(DO_NOT_SCAN)
    assertThat(result.getStatusFor("G8874VE")).isEqualTo(DO_NOT_SCAN)
  }

  private fun List<LimitStatusResponse>.getStatusFor(number: String) =
    this.find { it.prisonNumber == number }?.getBodyScanStatus()

  @Test
  fun `get limit status for year and when no prison numbers`() {
    val prisonNumbers = listOf<String>()
    val year = Year.of(2022)

    limitStatusService.getLimitStatusForYearAndPrisonNumbers(year, prisonNumbers)
    verifyNoMoreInteractions(bodyScanPrisonApiClient)
  }

  @Test
  fun `get limit status for year and prison numbers where no data in Nomis`() {
    val prisonNumbers = listOf("G8266VG", "G8874VT")
    val year = Year.of(2022)
    whenever(bodyScanPrisonApiClient.getPersonalCareNeedsForPrisonNumbers(any(), any(), any(), any())).thenReturn(
      listOf()
    )
    val result = limitStatusService.getLimitStatusForYearAndPrisonNumbers(year, prisonNumbers)
    verify(bodyScanPrisonApiClient).getPersonalCareNeedsForPrisonNumbers(
      "BSCAN", LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31), prisonNumbers
    )

    assertThat(result.getStatusFor("G8266VG")).isEqualTo(OK_TO_SCAN)
    assertThat(result.getStatusFor("G8874VT")).isEqualTo(OK_TO_SCAN)
  }
}
