package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.stream.Stream

@Repository
interface ConfirmedArrivalRepository : JpaRepository<ConfirmedArrival, Long> {

  fun findAllByArrivalDateAndPrisonId(
    arrivalDate: LocalDate,
    prisonId: String
  ): List<ConfirmedArrival>

  fun findAllByArrivalDateIsBetween(
    fromArrivalDate: LocalDate,
    toArrivalDate: LocalDate
  ): Stream<ConfirmedArrival>
}
