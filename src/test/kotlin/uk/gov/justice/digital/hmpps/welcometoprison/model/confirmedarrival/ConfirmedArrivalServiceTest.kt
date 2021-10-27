package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import io.mockk.every
import io.mockk.mockk
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

    every { confirmedArrivalRepository.findAllByArrivalDateAndPrisonNumber(any(), any()) } returns emptyList()

    val arrivals = confirmedArrivalService.extractConfirmedArrivalFromArrivals("MDI", date, listOf(arrival))

    assertThat(arrivals).hasSize(1)
  }

  @Test
  fun `remove from arrival when booking date, movement Id, prison id, and move type found in booking`() {

    every { confirmedArrivalRepository.findAllByArrivalDateAndPrisonNumber(any(), any()) } returns listOf(
      confirmedArrival
    )

    val arrivals = confirmedArrivalService.extractConfirmedArrivalFromArrivals("MDI", date, listOf(arrival))

    assertThat(arrivals).hasSize(0)
  }

  companion object {
    private val date = LocalDate.of(2021, 1, 2)

    private const val PRISON_NUMBER = "A1234AA"
    private const val ANOTHER_PRISON_NUMBER = "A1234BB"
    private const val PNC_NUMBER = "99/123456J"
    private const val ANOTHER_PNC_NUMBER = "11/123456J"

    private val arrival = Arrival(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1991, 7, 31),
      prisonNumber = "A1234AA",
      pncNumber = "99/123456J",
      date = date,
      fromLocation = "MDI",
      fromLocationType = LocationType.CUSTODY_SUITE
    )

    private val confirmedArrival = ConfirmedArrival(
      id = null,
      prisonNumber = "A1234AA",
      movementId = "1",
      timestamp = LocalDateTime.now(),
      arrivalType = ArrivalType.NEW_TO_PRISON,
      prisonId = "99/123456J",
      bookingId = 123,
      arrivalDate = date,
    )
  }
}
