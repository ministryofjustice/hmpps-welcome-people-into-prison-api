package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.LocationType
import java.time.LocalDate
import java.time.LocalDateTime

class ConfirmedArrivalServiceTest {

  private val confirmedArrivalRepository: ConfirmedArrivalRepository = mock()

  private val confirmedArrivalService: ConfirmedArrivalService = ConfirmedArrivalService(confirmedArrivalRepository)

  @Test
  fun `get arrivals when booking are empty`() {

    whenever(confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(any(), any())).thenReturn(emptyList())

    val arrivals = confirmedArrivalService.extractConfirmedArrivalFromArrivals("MDI", DATE, listOf(arrival))

    assertThat(arrivals).hasSize(1)
  }

  @Test
  fun `remove from arrival when booking date and movement Id found in booking`() {

    whenever(confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(any(), any())).thenReturn(
      listOf(confirmedArrival)
    )

    val arrivals = confirmedArrivalService.extractConfirmedArrivalFromArrivals("MDI", DATE, listOf(arrival))

    assertThat(arrivals).hasSize(0)
  }

  @Test
  fun `add new confirmed arrival`() {

    whenever(confirmedArrivalRepository.save(any())).thenReturn(confirmedArrivalDb)

    confirmedArrivalService.add(
      movementId = "MDI",
      prisonNumber = PRISON_NUMBER,
      prisonId = PNC_NUMBER,
      bookingId = 1,
      arrivalDate = LocalDate.of(2021, 1, 1),
      arrivalType = ArrivalType.NEW_TO_PRISON
    )
    verify(confirmedArrivalRepository).save(any())
  }

  companion object {
    private val DATE = LocalDate.of(2021, 1, 2)

    private const val PRISON_NUMBER = "A1234AA"
    private const val PNC_NUMBER = "99/123456J"
    private const val BOOKING_ID = 123L
    private const val MOVEMENT_ID = "1"

    private val arrival = Arrival(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1991, 7, 31),
      prisonNumber = "A1234AA",
      pncNumber = "99/123456J",
      date = DATE,
      fromLocation = "Moorland (HMP & YOI)",
      fromLocationId = "MDI",
      fromLocationType = LocationType.CUSTODY_SUITE
    )

    private val confirmedArrival = ConfirmedArrival(
      id = null,
      prisonNumber = PRISON_NUMBER,
      movementId = MOVEMENT_ID,
      timestamp = LocalDateTime.now(),
      arrivalType = ArrivalType.NEW_TO_PRISON,
      prisonId = PNC_NUMBER,
      bookingId = BOOKING_ID,
      arrivalDate = DATE,
    )
    private val confirmedArrivalDb = ConfirmedArrival(
      id = 1,
      prisonNumber = PRISON_NUMBER,
      movementId = MOVEMENT_ID,
      timestamp = LocalDateTime.now(),
      arrivalType = ArrivalType.NEW_TO_PRISON,
      prisonId = PNC_NUMBER,
      bookingId = BOOKING_ID,
      arrivalDate = DATE,
    )
  }
}
