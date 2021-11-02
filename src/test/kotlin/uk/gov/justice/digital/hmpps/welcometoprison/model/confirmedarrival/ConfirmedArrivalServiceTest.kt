package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.LocationType
import java.time.LocalDate
import java.time.LocalDateTime

class ConfirmedArrivalServiceTest {

  private val confirmedArrivalRepository: ConfirmedArrivalRepository = mockk()

  private val confirmedArrivalService: ConfirmedArrivalService = ConfirmedArrivalService(confirmedArrivalRepository)

  @Test
  fun `get arrivals when booking are empty`() {

    every { confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(any(), any()) } returns emptyList()

    val arrivals = confirmedArrivalService.extractConfirmedArrivalFromArrivals("MDI", DATE, listOf(arrival))

    assertThat(arrivals).hasSize(1)
  }

  @Test
  fun `remove from arrival when booking date and movement Id found in booking`() {

    every { confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(any(), any()) } returns listOf(
      confirmedArrival
    )

    val arrivals = confirmedArrivalService.extractConfirmedArrivalFromArrivals("MDI", DATE, listOf(arrival))

    assertThat(arrivals).hasSize(0)
  }

  @Test
  fun `add new confirmed arrival`() {

    every { confirmedArrivalRepository.save(any()) } returns confirmedArrivalDb

    confirmedArrivalService.add(
      movementId = "MDI",
      prisonNumber = PRISON_NUMBER,
      prisonId = PNC_NUMBER,
      bookingId = 1,
      arrivalDate = LocalDate.of(2021, 1, 1),
      arrivalType = ArrivalType.NEW_TO_PRISON
    )
    verify(exactly = 1) { confirmedArrivalRepository.save(any()) }
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
      fromLocation = "MDI",
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
