package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ArrivalsService.Companion.isMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ArrivalsService.Companion.toPotentialMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalRepository
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ArrivalsServiceTest {
  private val prisonService: PrisonService = mock()
  private val basmService: BasmService = mock()
  private val prisonerSearchService: PrisonerSearchService = mock()
  private val confirmedArrivalRepository: ConfirmedArrivalRepository = mock()
  private val locationFormatter: LocationFormatter = mock()

  private val confirmedArrivalService: ConfirmedArrivalService = ConfirmedArrivalService(confirmedArrivalRepository)
  private val arrivalsService =
    ArrivalsService(basmService, prisonService, prisonerSearchService, confirmedArrivalService, locationFormatter, FIXED_CLOCK)

  @Test
  fun getArrivals() {
    val noMatch = match.copy(pncNumber = "NA", prisonerNumber = "NA")
    val match = match.copy(pncNumber = arrival1.pncNumber, prisonerNumber = arrival1.prisonNumber)

    whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival1, arrival2))
    whenever(prisonerSearchService.getCandidateMatches(any()))
      .thenReturn(listOf(noMatch, match))
      .thenReturn(emptyList())

    val arrivals = arrivalsService.getArrivals("MDI", DATE)

    assertThat(arrivals).isEqualTo(
      listOf(
        arrival1.copy(potentialMatches = listOf(match.toPotentialMatch())),
        arrival2.copy(potentialMatches = emptyList())
      )
    )
  }

  @Test
  fun `getArrivals will remove duplicate search results`() {
    val match = match.copy(pncNumber = arrival1.pncNumber, prisonerNumber = arrival1.prisonNumber)

    whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival1))
    whenever(prisonerSearchService.getCandidateMatches(any()))
      .thenReturn(listOf(match, match))

    val arrivals = arrivalsService.getArrivals("MDI", DATE)

    assertThat(arrivals).isEqualTo(
      listOf(
        arrival1.copy(potentialMatches = listOf(match.toPotentialMatch()))
      )
    )
  }

  @Nested
  inner class TestIsCurrentPrisoner {
    @Test
    fun `all matches return current prisoner`() {
      val match1 = match.copy(pncNumber = arrival1.pncNumber, prisonerNumber = "A1234AA", status = "ACTIVE OUT")
      val match2 = match.copy(pncNumber = arrival1.pncNumber, prisonerNumber = "A1234BB", status = "ACTIVE OUT")
      val arrival = arrival1.copy(prisonNumber = null)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival))
      whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(listOf(match1, match2))

      val arrivals = arrivalsService.getArrivals("MDI", DATE)

      assertThat(arrivals).isEqualTo(
        listOf(
          arrival.copy(
            isCurrentPrisoner = true,
            potentialMatches = listOf(match1.toPotentialMatch(), match2.toPotentialMatch())
          ),
        )
      )
    }

    @Test
    fun `all matches return inactive prisoner`() {
      val match1 = match.copy(pncNumber = arrival1.pncNumber, prisonerNumber = "A1234AA", status = INACTIVE_OUT)
      val match2 = match.copy(pncNumber = arrival1.pncNumber, prisonerNumber = "A1234BB", status = INACTIVE_OUT)
      val arrival = arrival1.copy(prisonNumber = null)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival))
      whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(listOf(match1, match2))

      val arrivals = arrivalsService.getArrivals("MDI", DATE)

      assertThat(arrivals).isEqualTo(
        listOf(
          arrival.copy(
            isCurrentPrisoner = false,
            potentialMatches = listOf(match1.toPotentialMatch(), match2.toPotentialMatch())
          ),
        )
      )
    }

    @Test
    fun `some matches return inactive prisoner`() {
      val match1 = match.copy(pncNumber = arrival1.pncNumber, prisonerNumber = "A1234AA", status = "ACTIVE IN")
      val match2 = match.copy(pncNumber = arrival1.pncNumber, prisonerNumber = "A1234BB", status = INACTIVE_OUT)
      val arrival = arrival1.copy(prisonNumber = null)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival))
      whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(listOf(match1, match2))

      val arrivals = arrivalsService.getArrivals("MDI", DATE)

      assertThat(arrivals).isEqualTo(
        listOf(
          arrival.copy(
            isCurrentPrisoner = false,
            potentialMatches = listOf(match1.toPotentialMatch(), match2.toPotentialMatch())
          ),
        )
      )
    }

    @Test
    fun `no matches return inactive prisoner`() {
      val arrival = arrival1.copy(prisonNumber = null)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrival))
      whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(emptyList())

      val arrivals = arrivalsService.getArrivals("MDI", DATE)

      assertThat(arrivals).isEqualTo(
        listOf(
          arrival.copy(
            isCurrentPrisoner = false,
            potentialMatches = listOf()
          ),
        )
      )
    }
  }

  @Nested
  inner class TestMovementMatching {
    @Test
    fun `Both fields present from BASM move`() {
      val move = arrival1.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER))).isTrue
      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER, pncNumber = ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(match.copy(prisonerNumber = ANOTHER_PRISON_NUMBER, pncNumber = PNC_NUMBER))).isFalse
    }

    @Test
    fun `PNC absent from BASM move`() {
      val move = arrival1.copy(prisonNumber = PRISON_NUMBER, pncNumber = null)

      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER))).isTrue
      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER, pncNumber = ANOTHER_PNC_NUMBER))).isTrue
      assertThat(move.isMatch(match.copy(prisonerNumber = ANOTHER_PRISON_NUMBER, pncNumber = PNC_NUMBER))).isFalse
    }

    @Test
    fun `prison number absent from BASM move`() {
      val move = arrival1.copy(prisonNumber = null, pncNumber = PNC_NUMBER)

      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER))).isTrue
      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER, pncNumber = ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(match.copy(prisonerNumber = ANOTHER_PRISON_NUMBER, pncNumber = PNC_NUMBER))).isTrue
    }

    @Test
    fun `prison number and PNC absent from BASM move`() {
      val move = arrival1.copy(prisonNumber = null, pncNumber = null)

      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER))).isFalse
      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER, pncNumber = ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(match.copy(prisonerNumber = ANOTHER_PRISON_NUMBER, pncNumber = PNC_NUMBER))).isFalse
    }

    @Test
    fun `Case insensitive pnc match`() {
      val move = arrival1.copy(pncNumber = PNC_NUMBER.lowercase())
      assertThat(move.isMatch(match.copy(pncNumber = PNC_NUMBER))).isTrue
    }

    @Test
    fun `Case insensitive prisonNumber match`() {
      val move = arrival1.copy(prisonNumber = PRISON_NUMBER.lowercase())
      assertThat(move.isMatch(match.copy(prisonerNumber = PRISON_NUMBER))).isTrue
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

    private val FIXED_NOW: Instant = Instant.now()
    private val ZONE_ID: ZoneId = ZoneId.systemDefault()
    private val FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZONE_ID)

    private val arrival1 = Arrival(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = DOB,
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      date = DATE,
      fromLocation = "MDI",
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

    private val match = MatchPrisonerResponse(
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1980, 2, 23),
      prisonerNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      croNumber = CRO_NUMBER,
      status = INACTIVE_OUT
    )
  }
}
