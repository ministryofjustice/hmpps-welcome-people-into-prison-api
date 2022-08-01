package uk.gov.justice.digital.hmpps.bodyscan.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanPrisonApiClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareCounter
import uk.gov.justice.digital.hmpps.bodyscan.model.LimitStatusResponse
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
      "BSCAN",
      LocalDate.of(2022, 1, 1),
      LocalDate.of(2022, 12, 31),
      prisonNumbers
    )

    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8266VA") }.findFirst().get()
        .getBodyScanStatus()
    ).isEqualTo(LimitStatusResponse.BodyScanStatus.OK_TO_SCAN)
    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8874VB") }.findFirst().get()
        .getBodyScanStatus()
    ).isEqualTo(LimitStatusResponse.BodyScanStatus.CLOSE_TO_LIMIT)
    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8874VC") }.findFirst().get()
        .getBodyScanStatus()
    ).isEqualTo(LimitStatusResponse.BodyScanStatus.CLOSE_TO_LIMIT)
    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8874VD") }.findFirst().get()
        .getBodyScanStatus()
    ).isEqualTo(LimitStatusResponse.BodyScanStatus.DO_NOT_SCAN)
    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8874VE") }.findFirst().get()
        .getBodyScanStatus()
    ).isEqualTo(LimitStatusResponse.BodyScanStatus.DO_NOT_SCAN)
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
      "BSCAN",
      LocalDate.of(2022, 1, 1),
      LocalDate.of(2022, 12, 31),
      prisonNumbers
    )

    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8266VG") }.findFirst().get()
        .getBodyScanStatus()
    ).isEqualTo(LimitStatusResponse.BodyScanStatus.OK_TO_SCAN)
    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8874VT") }.findFirst().get()
        .getBodyScanStatus()
    ).isEqualTo(LimitStatusResponse.BodyScanStatus.OK_TO_SCAN)
  }
}
