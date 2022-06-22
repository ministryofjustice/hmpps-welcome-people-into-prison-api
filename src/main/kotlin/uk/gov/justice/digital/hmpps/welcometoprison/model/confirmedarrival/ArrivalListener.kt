package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.config.SecurityUserContext
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional
class ArrivalListener(
  private val confirmedArrivalRepository: ConfirmedArrivalRepository,
  private val securityUserContext: SecurityUserContext,
  private val clock: Clock
) {
  fun arrived(event: ArrivalEvent) {
    val confirmedArrival = event.toConfirmedArrival(securityUserContext.principal, clock)
    confirmedArrivalRepository.save(confirmedArrival)
  }
}

data class ArrivalEvent(
  val movementId: String?,
  val prisonId: String,
  val prisonNumber: String,
  val bookingId: Long,
  val arrivalType: ArrivalType,
) {
  fun toConfirmedArrival(username: String, clock: Clock) = ConfirmedArrival(
    movementId = movementId,
    prisonNumber = prisonNumber,
    timestamp = LocalDateTime.now(clock),
    arrivalType = arrivalType,
    prisonId = prisonId,
    bookingId = bookingId,
    arrivalDate = LocalDate.now(clock),
    username = username,
  )
}
