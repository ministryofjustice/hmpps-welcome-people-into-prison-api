package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals

import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.refEq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.config.SecurityUserContext
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType.RECALL
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ArrivalListenerTest {
  private val fixedNow = Instant.now()
  private val zoneId = ZoneId.systemDefault()
  private val fixedClock = Clock.fixed(fixedNow, zoneId)
  private val confirmedArrivalRepository: ConfirmedArrivalRepository = mock()
  private val securityUserContext: SecurityUserContext = mock()
  private val telemetryClient: TelemetryClient = mock()

  private val arrivalListener =
    ArrivalListener(confirmedArrivalRepository, telemetryClient, securityUserContext, fixedClock)

  @Test
  fun `arrived events are persisted in the DB`() {
    whenever(securityUserContext.principal).thenReturn("USER-1")

    arrivalListener.arrived(
      ArrivalEvent(
        arrivalId = "1",
        prisonId = "MDI",
        prisonNumber = "A1234AA",
        bookingId = 123,
        arrivalType = RECALL,
      ),
    )

    verify(confirmedArrivalRepository).save(
      refEq(
        ConfirmedArrival(
          arrivalId = "1",
          prisonId = "MDI",
          prisonNumber = "A1234AA",
          bookingId = 123,
          arrivalType = RECALL,
          username = "USER-1",
          arrivalDate = LocalDate.now(fixedClock),
          timestamp = LocalDateTime.now(fixedClock),
        ),
      ),
    )
  }

  @Test
  fun `arrived events are recorded in App insights`() {
    whenever(securityUserContext.principal).thenReturn("USER-1")

    arrivalListener.arrived(
      ArrivalEvent(
        arrivalId = "1",
        prisonId = "MDI",
        prisonNumber = "A1234AA",
        bookingId = 123,
        arrivalType = RECALL,
      ),
    )

    verify(telemetryClient).trackEvent(
      "Arrival",
      mapOf(
        "arrivalId" to "1",
        "prisonId" to "MDI",
        "prisonNumber" to "A1234AA",
        "bookingId" to "123",
        "arrivalType" to "RECALL",
        "username" to "USER-1",
        "timestamp" to LocalDateTime.now(fixedClock).toString(),
      ),
      null,
    )
  }
}
