package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.refEq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.config.SecurityUserContext
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalType.RECALL
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrivalRepository
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ArrivalListenerTest {
  private val FIXED_NOW = Instant.now()
  private val ZONE_ID = ZoneId.systemDefault()
  private val FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZONE_ID)
  private val confirmedArrivalRepository: ConfirmedArrivalRepository = mock()
  private val securityUserContext: SecurityUserContext = mock()

  private val arrivalListener = ArrivalListener(confirmedArrivalRepository, securityUserContext, FIXED_CLOCK)

  @Test
  fun arrived() {

    whenever(securityUserContext.principal).thenReturn("USER-1")

    arrivalListener.arrived(
      ArrivalEvent(
        movementId = "1", prisonId = "MDI", prisonNumber = "A1234AA", bookingId = 123, arrivalType = RECALL
      )
    )

    verify(confirmedArrivalRepository).save(
      refEq(
        ConfirmedArrival(
          movementId = "1",
          prisonId = "MDI",
          prisonNumber = "A1234AA",
          bookingId = 123,
          arrivalType = RECALL,
          username = "USER-1",
          arrivalDate = LocalDate.now(FIXED_CLOCK),
          timestamp = LocalDateTime.now(FIXED_CLOCK)
        )
      )
    )
  }
}
