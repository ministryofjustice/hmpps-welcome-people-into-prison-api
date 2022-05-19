package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import java.time.LocalDate
import java.time.LocalTime

@Service
class RecentArrivalsService(
  private val prisonApiClient: PrisonApiClient
) {
  fun getArrivals(
    prisonId: String,
    fromDate: LocalDate,
    toDate: LocalDate,
    pageSize: Int,
    page: Int
  ): Page<RecentArrival> {

    val pageRequest = PageRequest.of(page, pageSize)

    val result = prisonApiClient.getMovement(prisonId, fromDate.atStartOfDay(), toDate.atTime(LocalTime.MAX))
    val start = pageSize * page
    val end = Math.min((pageSize * page) + pageSize, result.size)

    if (start > end) {
      return PageImpl(ArrayList<RecentArrival>(), pageRequest, result.size.toLong())
    }

    var recentArrivals = result.subList(start, end).map {
      RecentArrival(
        it.offenderNo,
        it.dateOfBirth,
        it.firstName,
        it.lastName,
        it.movementDateTime,
        it.location
      )
    }

    return PageImpl(recentArrivals, pageRequest, result.size.toLong())
  }
}
