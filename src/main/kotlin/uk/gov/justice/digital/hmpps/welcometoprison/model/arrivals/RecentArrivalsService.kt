package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.SearchByNameAndPrisonNumber
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Searcher
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import java.time.LocalDate
import java.time.LocalTime

@Service
class RecentArrivalsService(
  private val prisonApiClient: PrisonApiClient,
  private val locationFormatter: LocationFormatter,
  private val searcher: Searcher<String, RecentArrival> = Searcher(SearchByNameAndPrisonNumber())
) {

  fun getArrivals(
    prisonId: String,
    dateRange: Pair<LocalDate, LocalDate>,
    page: PageRequest,
    query: String? = null
  ): Page<RecentArrival> {

    val (fromDate, toDate) = dateRange
    val movements = prisonApiClient.getMovement(prisonId, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    val start = page.pageSize * page.pageNumber
    val end = (start + page.pageSize).coerceAtMost(movements.size)

    if (start > end) {
      return PageImpl(ArrayList<RecentArrival>(), page, movements.size.toLong())
    }

    val arrivals = movements.map {
      RecentArrival(
        it.offenderNo,
        it.dateOfBirth,
        it.firstName,
        it.lastName,
        it.movementDateTime,
        it.location?.let { loc -> locationFormatter.format(loc) }
      )
    }

    val filteredArrivals = searcher.search(query, arrivals)
    val arrivalPage = filteredArrivals.subList(start, end.coerceAtMost(filteredArrivals.size))

    return PageImpl(arrivalPage, page, filteredArrivals.size.toLong())
  }
}
