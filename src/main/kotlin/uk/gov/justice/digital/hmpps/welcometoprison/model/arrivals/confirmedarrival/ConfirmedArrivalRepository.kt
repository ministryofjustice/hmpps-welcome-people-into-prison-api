package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ConfirmedArrivalRepository : JpaRepository<ConfirmedArrival, Long> {

  fun findAllByArrivalDateAndPrisonId(
    arrivalDate: LocalDate,
    prisonId: String
  ): List<ConfirmedArrival>
}
