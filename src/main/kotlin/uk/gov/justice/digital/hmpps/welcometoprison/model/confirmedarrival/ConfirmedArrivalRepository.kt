package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ConfirmedArrivalRepository : JpaRepository<ConfirmedArrival, Long> {

  fun findAllByArrivalDateAndPrisonNumber(
    arrivalDate: LocalDate,
    prisonNumber: String
  ): List<ConfirmedArrival>
}
