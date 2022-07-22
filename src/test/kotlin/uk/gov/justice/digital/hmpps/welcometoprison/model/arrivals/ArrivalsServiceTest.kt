package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.context.properties.bind.Bindable.listOf
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrivalRepository
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Stream

class ArrivalsServiceTest {
  private val basmService: BasmService = mock()
  private val prisonerSearchService: PrisonerSearchService = mock()
  private val confirmedArrivalRepository: ConfirmedArrivalRepository = mock()
  private val confirmedArrivalsCsvConverter: ConfirmedArrivalsCsvConverter = ConfirmedArrivalsCsvConverter()

  private val arrivalsService =
    ArrivalsService(basmService, prisonerSearchService, confirmedArrivalRepository, confirmedArrivalsCsvConverter)

  @Test
  fun `Retrieving csv arrivals`() {

    whenever(confirmedArrivalRepository.findAllByArrivalDateIsBetween(any(), any())).thenReturn(
      Stream.of(
        confirmedArrival
      )
    )

    val arrivals = arrivalsService.getArrivalsAsCsv(DATE, 7)
    assertThat(arrivals).isEqualTo(
      "id,timestamp,arrivalDate,prisonId,arrivalType,username\n" +
        "1,2021-02-23T01:00:00,2021-02-01,MDI,NEW_TO_PRISON,\"9fa70e9f0d541dab23d0ca65edb1a261\"\n"
    )
  }

  @Test
  fun `Retrieving arrivals`() {
    val match = match.copy(pncNumber = arrival1.pncNumber, prisonNumber = arrival1.prisonNumber!!)

    whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival1, arrival2))
    whenever(prisonerSearchService.findPotentialMatches(any())).thenReturn(listOf(match)).thenReturn(emptyList())

    val arrivals = arrivalsService.getArrivals("MDI", DATE)

    assertThat(arrivals).isEqualTo(
      listOf(
        arrival1.copy(potentialMatches = listOf(match)), arrival2.copy(potentialMatches = emptyList())
      )
    )
  }

  @Test
  fun `Arrivals are removed when confirmed`() {

    whenever(basmService.getArrivals(any(), any(), any())).thenReturn(listOf(arrival1, arrival2))
    whenever(confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(any(), any())).thenReturn(
      listOf(arrival1.whenConfirmed())
    )

    val arrivals = arrivalsService.getArrivals("MDI", DATE)

    assertThat(arrivals).hasSize(1)
    assertThat(arrivals[0].id).isEqualTo(arrival2.id)
  }

  @Nested
  inner class TestIsCurrentPrisoner {
    @Test
    fun `all matches return current prisoner`() {
      val match1 = match.copy(pncNumber = arrival1.pncNumber, prisonNumber = "A1234AA", isCurrentPrisoner = true)
      val match2 = match.copy(pncNumber = arrival1.pncNumber, prisonNumber = "A1234BB", isCurrentPrisoner = true)
      val arrival = arrival1.copy(prisonNumber = null)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival))
      whenever(prisonerSearchService.findPotentialMatches(any())).thenReturn(listOf(match1, match2))

      val arrivals = arrivalsService.getArrivals("MDI", DATE)

      assertThat(arrivals).isEqualTo(
        listOf(
          arrival.copy(
            isCurrentPrisoner = true, potentialMatches = listOf(match1, match2)
          ),
        )
      )
    }

    @Test
    fun `all matches return inactive prisoner`() {
      val match1 = match.copy(pncNumber = arrival1.pncNumber, prisonNumber = "A1234AA", isCurrentPrisoner = false)
      val match2 = match.copy(pncNumber = arrival1.pncNumber, prisonNumber = "A1234BB", isCurrentPrisoner = false)
      val arrival = arrival1.copy(prisonNumber = null)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival))
      whenever(prisonerSearchService.findPotentialMatches(any())).thenReturn(listOf(match1, match2))

      val arrivals = arrivalsService.getArrivals("MDI", DATE)

      assertThat(arrivals).isEqualTo(
        listOf(
          arrival.copy(
            isCurrentPrisoner = false, potentialMatches = listOf(match1, match2)
          ),
        )
      )
    }

    @Test
    fun `some matches return inactive prisoner`() {
      val match1 = match.copy(pncNumber = arrival1.pncNumber, prisonNumber = "A1234AA", isCurrentPrisoner = true)
      val match2 = match.copy(pncNumber = arrival1.pncNumber, prisonNumber = "A1234BB", isCurrentPrisoner = false)
      val arrival = arrival1.copy(prisonNumber = null)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival))
      whenever(prisonerSearchService.findPotentialMatches(any())).thenReturn(listOf(match1, match2))

      val arrivals = arrivalsService.getArrivals("MDI", DATE)

      assertThat(arrivals).isEqualTo(
        listOf(
          arrival.copy(
            isCurrentPrisoner = false, potentialMatches = listOf(match1, match2)
          ),
        )
      )
    }

    @Test
    fun `no matches return inactive prisoner`() {
      val arrival = arrival1.copy(prisonNumber = null)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival))
      whenever(prisonerSearchService.findPotentialMatches(any())).thenReturn(emptyList())

      val arrivals = arrivalsService.getArrivals("MDI", DATE)

      assertThat(arrivals).isEqualTo(
        listOf(
          arrival.copy(
            isCurrentPrisoner = false, potentialMatches = listOf()
          ),
        )
      )
    }
  }

  companion object {
    private val DATE = LocalDate.of(2021, 1, 2)
    private const val PRISON_NUMBER = "A1234AA"
    private const val ANOTHER_PRISON_NUMBER = "A1234BB"
    private const val PNC_NUMBER = "99/123456J"
    private const val CRO_NUMBER = "SF80/655108T"
    private const val ANOTHER_PNC_NUMBER = "11/123456J"
    private val DOB = LocalDate.of(1980, 2, 23)

    private val confirmedArrival = ConfirmedArrival(
      id = 1,
      prisonNumber = "ADF123",
      movementId = "qweqewqwe123-123123wqw-12312312",
      timestamp = LocalDateTime.of(2021, 2, 23, 1, 0, 0),
      arrivalType = ArrivalType.NEW_TO_PRISON,
      prisonId = "MDI",
      bookingId = 1232,
      arrivalDate = LocalDate.of(2021, 2, 1),
      username = "UserT"
    )

    private val arrival1 = Arrival(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = DOB,
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      date = DATE,
      fromLocation = "Moorland (HMP & YOI)",
      fromLocationId = "MDI",
      fromLocationType = LocationType.CUSTODY_SUITE,
      isCurrentPrisoner = false
    )

    private val arrival2 = arrival1.copy(
      id = "2",
      firstName = "First",
      lastName = "Last",
      dateOfBirth = DOB,
      prisonNumber = ANOTHER_PRISON_NUMBER,
      pncNumber = ANOTHER_PNC_NUMBER,
      fromLocationType = LocationType.PRISON,
      isCurrentPrisoner = false
    )

    private val match = PotentialMatch(
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1980, 2, 23),
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      croNumber = CRO_NUMBER,
      sex = "Female",
      isCurrentPrisoner = false
    )

    fun Arrival.whenConfirmed() = ConfirmedArrival(
      id = null,
      prisonNumber = this.prisonNumber!!,
      movementId = this.id!!,
      timestamp = LocalDateTime.now(),
      arrivalType = ArrivalType.NEW_TO_PRISON,
      prisonId = "Prison Id",
      bookingId = 123,
      arrivalDate = this.date,
      username = "user_1"
    )
  }
}
