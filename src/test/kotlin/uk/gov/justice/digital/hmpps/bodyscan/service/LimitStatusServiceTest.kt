package uk.gov.justice.digital.hmpps.bodyscan.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanApiClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareCounter
import uk.gov.justice.digital.hmpps.bodyscan.model.LimitStatusResponse
import java.time.LocalDate
import java.time.Year

class LimitStatusServiceTest {
  private val prisonApiClient: BodyScanApiClient = mock()
  private val limitStatusService = LimitStatusService(prisonApiClient)

  @Test
  fun `get limit status for year and prison numbers`() {
    val prisonNumbers = listOf("G8266VG", "G8874VT", "G8874VZ")
    val year = Year.of(2022)
    whenever(prisonApiClient.getPersonalCareNeedsForPrisonNumbers(any(), any(), any(), any())).thenReturn(
      listOf(
        PersonalCareCounter("G8266VG", 1),
        PersonalCareCounter("G8874VT", 120),
        PersonalCareCounter("G8874VZ", 160)
      )
    )
    val result = limitStatusService.getLimitStatusForYearAndPrisonNumbers(year, prisonNumbers)
    verify(prisonApiClient).getPersonalCareNeedsForPrisonNumbers(
      "BSCAN",
      LocalDate.of(2022, 1, 1),
      LocalDate.of(2022, 12, 31),
      prisonNumbers
    )

    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8266VG") }.findFirst().get().bodyScanStatus.equals(
        LimitStatusResponse.BodyScanStatus.OK_TO_SCAN
      )
    )
    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8874VT") }.findFirst().get().bodyScanStatus.equals(
        LimitStatusResponse.BodyScanStatus.CLOSE_TO_LIMIT
      )
    )
    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8874VZ") }.findFirst().get().bodyScanStatus.equals(
        LimitStatusResponse.BodyScanStatus.DO_NOT_SCAN
      )
    )
  }

  @Test
  fun `get limit status for year and prison numbers where no data in Nomis`() {
    val prisonNumbers = listOf("G8266VG", "G8874VT")
    val year = Year.of(2022)
    whenever(prisonApiClient.getPersonalCareNeedsForPrisonNumbers(any(), any(), any(), any())).thenReturn(
      listOf()
    )
    val result = limitStatusService.getLimitStatusForYearAndPrisonNumbers(year, prisonNumbers)
    verify(prisonApiClient).getPersonalCareNeedsForPrisonNumbers(
      "BSCAN",
      LocalDate.of(2022, 1, 1),
      LocalDate.of(2022, 12, 31),
      prisonNumbers
    )

    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8266VG") }.findFirst().get().bodyScanStatus.equals(
        LimitStatusResponse.BodyScanStatus.OK_TO_SCAN
      )
    )
    Assertions.assertThat(
      result.stream().filter { it.prisonNumber == ("G8874VT") }.findFirst().get().bodyScanStatus.equals(
        LimitStatusResponse.BodyScanStatus.OK_TO_SCAN
      )
    )
  }
}
