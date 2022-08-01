package uk.gov.justice.digital.hmpps.bodyscan.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanApiClient
import uk.gov.justice.digital.hmpps.bodyscan.model.LimitStatusResponse
import java.time.LocalDate
import java.time.Year

@Service
class LimitStatusService(
  private val prisonApiClient: BodyScanApiClient
) {
  companion object {
    const val TYPE = "BSCAN"
  }

  fun getLimitStatusForYearAndPrisonNumbers(year: Year, prisonNumbers: List<String>): List<LimitStatusResponse> {
    val startDate: LocalDate = LocalDate.ofYearDay(year.value, 1)
    val endDate: LocalDate = LocalDate.ofYearDay(year.value, startDate.lengthOfYear())
    val apiMap = prisonApiClient.getPersonalCareNeedsForPrisonNumbers(TYPE, startDate, endDate, prisonNumbers)
      .associate { it.offenderNo to it.size }
    return prisonNumbers.stream().map {
      LimitStatusResponse(
        prisonNumber = it,
        numberOfBodyScans = apiMap.getOrDefault(it, 0)
      )
    }.toList()
  }
}
