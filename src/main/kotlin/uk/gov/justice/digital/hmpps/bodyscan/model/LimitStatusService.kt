package uk.gov.justice.digital.hmpps.bodyscan.model

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.BodyScanPrisonApiClient
import java.time.LocalDate
import java.time.Year
import java.time.temporal.TemporalAdjusters

@Service
class LimitStatusService(
  private val prisonApiClient: BodyScanPrisonApiClient,
) {
  companion object {
    const val TYPE = "BSCAN"
  }

  fun getLimitStatusForYearAndPrisonNumbers(year: Year, rawPrisonNumbers: List<String?>): List<LimitStatusResponse> {
    val prisonNumbers = rawPrisonNumbers.filterNotNull()
    if (prisonNumbers.isEmpty()) return listOf()
    val startDate: LocalDate = LocalDate.ofYearDay(year.value, 1)
    val endDate: LocalDate = startDate.with(TemporalAdjusters.lastDayOfYear())
    val apiMap = prisonApiClient.getPersonalCareNeedsForPrisonNumbers(TYPE, startDate, endDate, prisonNumbers)
      .associate { it.offenderNo to it.size }
    return prisonNumbers.stream().map {
      LimitStatusResponse(
        prisonNumber = it,
        numberOfBodyScans = apiMap.getOrDefault(it, 0),
      )
    }.toList()
  }
}
