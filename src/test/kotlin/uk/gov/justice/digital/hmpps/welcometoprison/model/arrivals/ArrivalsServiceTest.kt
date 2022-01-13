package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ArrivalsService.Companion.isMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalRepository
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ArrivalsServiceTest {
  private val prisonService: PrisonService = mock()
  private val basmService: BasmService = mock()
  private val prisonerSearchService: PrisonerSearchService = mock()

  // We need to mock repository instead of confirmedArrivalService as we have a problem to generate the list of prisoners with prisonNumber equals null
  private val confirmedArrivalRepository: ConfirmedArrivalRepository = mock()

  private val confirmedArrivalService: ConfirmedArrivalService = ConfirmedArrivalService(confirmedArrivalRepository)
  private val arrivalsService =
    ArrivalsService(basmService, prisonService, prisonerSearchService, confirmedArrivalService, FIXED_CLOCK)
  val result = { prisonNumber: String?, pnc: String? -> MatchPrisonerResponse(prisonNumber, pnc, "ACTIVE IN") }

  @Test
  fun `getArrivals - happy path`() {
    whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(basmOnlyArrival, arrivalKnownToNomis))
    whenever(confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(any(), any())).thenReturn(emptyList())
    whenever(prisonerSearchService.getCandidateMatches(any())).thenReturn(listOf(result("A1234AA", "99/123456J"), result("A1234BB", "11/123456J")))

    val arrivals = arrivalsService.getArrivals("MDI", DATE)

    assertThat(arrivals).isEqualTo(
      listOf(basmOnlyArrival, arrivalKnownToNomis)
    )
  }

  @Nested
  inner class MatchingArrivals {

    @BeforeEach
    fun before() {
      whenever(confirmedArrivalRepository.findAllByArrivalDateAndPrisonId(any(), any())).thenReturn(emptyList())
    }

    @Test
    fun `finds match from candidates`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(move))
      whenever(prisonerSearchService.getCandidateMatches(move)).thenReturn(
        listOf(
          result(move.prisonNumber, move.pncNumber)
        )
      )

      val movement = arrivalsService.getArrivals("MDI", DATE).first()

      assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(move.prisonNumber, move.pncNumber))
    }

    @Test
    fun `finds match from candidates when PNC number includes lower case character`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = "99/123456j")

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(move))

      whenever(prisonerSearchService.getCandidateMatches(move)).thenReturn(
        listOf(
          result(move.prisonNumber, "99/123456J")
        )
      )

      val movement = arrivalsService.getArrivals("MDI", DATE).first()

      assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(move.prisonNumber, "99/123456J"))
    }

    @Test
    fun `Doesn't find match from candidates due to prison number mismatch`() {

      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(move))
      whenever(prisonerSearchService.getCandidateMatches(move)).thenReturn(
        listOf(
          result(ANOTHER_PRISON_NUMBER, move.pncNumber)
        )
      )

      val movement = arrivalsService.getArrivals("MDI", DATE).first()

      assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(null, move.pncNumber))
    }

    @Test
    fun `Doesn't find match from candidates due to PNC mismatch`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(move))
      whenever(prisonerSearchService.getCandidateMatches(move)).thenReturn(
        listOf(
          result(move.prisonNumber, ANOTHER_PNC_NUMBER)
        )
      )

      val movement = arrivalsService.getArrivals("MDI", DATE).first()

      assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(null, movement.pncNumber))
    }

    @Test
    fun `Doesn't find match as no candidates for new prisoner`() {
      val move = basmOnlyArrival.copy(prisonNumber = null, pncNumber = PNC_NUMBER)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(move))
      whenever(prisonerSearchService.getCandidateMatches(move)).thenReturn(emptyList())

      val movement = arrivalsService.getArrivals("MDI", DATE).first()

      assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(null, movement.pncNumber))
    }

    @Test
    fun `Doesn't find match as no candidates when prison number provided`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

      whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(move))
      whenever(prisonerSearchService.getCandidateMatches(move)).thenReturn(emptyList())

      val movement = arrivalsService.getArrivals("MDI", DATE).first()

      assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(null, movement.pncNumber))
    }
  }

  @Nested
  inner class TestMovementMatching {
    @Test
    fun `Both fields present from BASM move`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

      assertThat(move.isMatch(result(PRISON_NUMBER, PNC_NUMBER))).isTrue
      assertThat(move.isMatch(result(PRISON_NUMBER, ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(result(ANOTHER_PRISON_NUMBER, PNC_NUMBER))).isFalse
    }

    @Test
    fun `PNC absent from BASM move`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = null)

      assertThat(move.isMatch(result(PRISON_NUMBER, PNC_NUMBER))).isTrue
      assertThat(move.isMatch(result(PRISON_NUMBER, ANOTHER_PNC_NUMBER))).isTrue
      assertThat(move.isMatch(result(ANOTHER_PRISON_NUMBER, PNC_NUMBER))).isFalse
    }

    @Test
    fun `prison number absent from BASM move`() {
      val move = basmOnlyArrival.copy(prisonNumber = null, pncNumber = PNC_NUMBER)

      assertThat(move.isMatch(result(PRISON_NUMBER, PNC_NUMBER))).isTrue
      assertThat(move.isMatch(result(PRISON_NUMBER, ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(result(ANOTHER_PRISON_NUMBER, PNC_NUMBER))).isTrue
    }

    @Test
    fun `prison number and PNC absent from BASM move`() {
      val move = basmOnlyArrival.copy(prisonNumber = null, pncNumber = null)

      assertThat(move.isMatch(result(PRISON_NUMBER, PNC_NUMBER))).isFalse
      assertThat(move.isMatch(result(PRISON_NUMBER, ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(result(ANOTHER_PRISON_NUMBER, PNC_NUMBER))).isFalse
    }
  }

  companion object {
    private val DATE = LocalDate.of(2021, 1, 2)

    private const val PRISON_NUMBER = "A1234AA"
    private const val ANOTHER_PRISON_NUMBER = "A1234BB"
    private const val PNC_NUMBER = "99/123456J"
    private const val ANOTHER_PNC_NUMBER = "11/123456J"
    private val DOB = LocalDate.of(1980, 2, 23)

    private val FIXED_NOW: Instant = Instant.now()
    private val ZONE_ID: ZoneId = ZoneId.systemDefault()
    private val FIXED_CLOCK = Clock.fixed(FIXED_NOW, ZONE_ID)

    private val basmOnlyArrival = Arrival(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = DOB,
      prisonNumber = PRISON_NUMBER,
      pncNumber = PNC_NUMBER,
      date = DATE,
      fromLocation = "MDI",
      fromLocationType = LocationType.CUSTODY_SUITE,
      isCurrentPrisoner = true
    )

    private val arrivalKnownToNomis = basmOnlyArrival.copy(
      id = "2",
      firstName = "First",
      lastName = "Last",
      dateOfBirth = DOB,
      prisonNumber = ANOTHER_PRISON_NUMBER,
      pncNumber = ANOTHER_PNC_NUMBER,
      fromLocationType = LocationType.PRISON,
      isCurrentPrisoner = true
    )
  }
}
