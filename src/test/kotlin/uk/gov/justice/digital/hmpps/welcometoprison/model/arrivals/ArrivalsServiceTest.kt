package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ArrivalsService.Companion.isMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.ArrivalsService.Companion.toPotentialMatch
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
  val result = { firstName: String, lastName: String, dateOfBirth: LocalDate, prisonNumber: String?, pnc: String? ->
    MatchPrisonerResponse(
      firstName,
      lastName,
      dateOfBirth,
      prisonNumber,
      pnc,
      "ACTIVE IN"
    )
  }

  @Test
  fun getArrivals() {
    val noMatch = result("JIM", "SMITH", LocalDate.of(1980, 2, 23), "NA", "NA")
    val match =
      result("JIM", "SMITH", LocalDate.of(1980, 2, 23), arrivalKnownToNomis.prisonNumber, arrivalKnownToNomis.pncNumber)

    whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(basmOnlyArrival, arrivalKnownToNomis))
    whenever(prisonerSearchService.getCandidateMatches(any()))
      .thenReturn(emptyList())
      .thenReturn(listOf(noMatch, match))

    val arrivals = arrivalsService.getArrivals("MDI", DATE)

    assertThat(arrivals).isEqualTo(
      listOf(
        basmOnlyArrival.copy(potentialMatches = emptyList()),
        arrivalKnownToNomis.copy(potentialMatches = listOf(match.toPotentialMatch()))
      )
    )
  }

  @Test
  fun `getArrivals will remove duplicate search results`() {
    val match =
      result("JIM", "SMITH", LocalDate.of(1980, 2, 23), arrivalKnownToNomis.prisonNumber, arrivalKnownToNomis.pncNumber)

    whenever(basmService.getArrivals("MDI", DATE, DATE)).thenReturn(listOf(arrivalKnownToNomis))
    whenever(prisonerSearchService.getCandidateMatches(any()))
      .thenReturn(listOf(match, match))

    val arrivals = arrivalsService.getArrivals("MDI", DATE)

    assertThat(arrivals).isEqualTo(
      listOf(
        arrivalKnownToNomis.copy(potentialMatches = listOf(match.toPotentialMatch()))
      )
    )
  }

  @Nested
  inner class TestMovementMatching {
    @Test
    fun `Both fields present from BASM move`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER))).isTrue
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, ANOTHER_PRISON_NUMBER, PNC_NUMBER))).isFalse
    }

    @Test
    fun `PNC absent from BASM move`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = null)

      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER))).isTrue
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, ANOTHER_PNC_NUMBER))).isTrue
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, ANOTHER_PRISON_NUMBER, PNC_NUMBER))).isFalse
    }

    @Test
    fun `prison number absent from BASM move`() {
      val move = basmOnlyArrival.copy(prisonNumber = null, pncNumber = PNC_NUMBER)

      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER))).isTrue
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, ANOTHER_PRISON_NUMBER, PNC_NUMBER))).isTrue
    }

    @Test
    fun `prison number and PNC absent from BASM move`() {
      val move = basmOnlyArrival.copy(prisonNumber = null, pncNumber = null)

      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER))).isFalse
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, ANOTHER_PNC_NUMBER))).isFalse
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, ANOTHER_PRISON_NUMBER, PNC_NUMBER))).isFalse
    }

    @Test
    fun `Case insensitive pnc match`() {
      val move = basmOnlyArrival.copy(pncNumber = PNC_NUMBER.lowercase())
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER))).isTrue
    }

    @Test
    fun `Case insensitive prisonNumber match`() {
      val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER.lowercase())
      assertThat(move.isMatch(result(FIRST_NAME, LAST_NAME, DOB, PRISON_NUMBER, PNC_NUMBER))).isTrue
    }
  }

  companion object {
    private val DATE = LocalDate.of(2021, 1, 2)
    private const val FIRST_NAME = "HARRY"
    private const val LAST_NAME = "SMITH"
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
