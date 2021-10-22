package uk.gov.justice.digital.hmpps.welcometoprison.model.booking

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.LocationType
import java.time.LocalDate
import java.time.LocalDateTime

class BookingServiceTest {

  private val bookingRepository: BookingRepository = mockk()

  private val bookingService: BookingService = BookingService(bookingRepository)

  @Test
  fun `getArrivals when booking are empty `() {

    every { bookingRepository.findAllByBookingDateAndPrisonId(any(), any()) } returns emptyList()

    val arrivals = bookingService.extractExistingBookingsFromArrivals("MDI", date, listOf(arrival))

    assertThat(arrivals.size).isEqualTo(1)
  }
  @Test
  fun `remove from arrival when booking date, movement Id, prison id, and move type found in booking`() {

    every { bookingRepository.findAllByBookingDateAndPrisonId(any(), any()) } returns listOf(booking)

    val arrivals = bookingService.extractExistingBookingsFromArrivals("MDI", date, listOf(arrival))

    assertThat(arrivals.size).isEqualTo(0)
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
      fromLocationType = LocationType.CUSTODY_SUITE,
      moveType = "PRISON_REMAND"
    )
    private val booking = Booking(
      id = null,
      prisonId = "A1234AA",
      movementId = "1",
      timestamp = LocalDateTime.now(),
      moveType = "PRISON_REMAND",
      prisonerId = "99/123456J",
      bookingId = "Booking Id",
      bookingDate = date,
    )
  }
}
