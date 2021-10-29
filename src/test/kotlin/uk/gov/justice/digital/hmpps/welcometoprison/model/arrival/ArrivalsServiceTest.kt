package uk.gov.justice.digital.hmpps.welcometoprison.model.arrival

import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.ArrivalsService.Companion.isMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrivalRepository
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.AdmitArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

class ArrivalsServiceTest {
  private val prisonService: PrisonService = mockk()
  private val basmService: BasmService = mockk()
  private val prisonerSearchService: PrisonerSearchService = mockk()
  private val confirmedArrivalRepository: ConfirmedArrivalRepository = mockk()

  private val confirmedArrivalService: ConfirmedArrivalService = ConfirmedArrivalService(confirmedArrivalRepository)
  private val arrivalsService = ArrivalsService(basmService, prisonService, prisonerSearchService, confirmedArrivalService)
  val result = { prisonNumber: String?, pnc: String? -> MatchPrisonerResponse(prisonNumber, pnc, "ACTIVE IN") }

  @Test
   fun `getMoves - happy path`() {
     every { basmService.getArrivals("MDI", date, date) } returns listOf(basmOnlyArrival)
     every { prisonService.getTransfers("MDI", date) } returns listOf(arrivalKnownToNomis)
    every { confirmedArrivalRepository.findAllByArrivalDateAndPrisonNumber(any(), any()) } returns emptyList()
     every { prisonerSearchService.getCandidateMatches(any()) } returns listOf(result("A1234AA", "99/123456J"))

     val moves = arrivalsService.getMovements("MDI", date)

     assertThat(moves).isEqualTo(
       listOf(basmOnlyArrival, arrivalKnownToNomis)
     )
   }

   @Nested
   inner class `Matching Movements` {

     @BeforeEach
     fun before() {
       every { prisonService.getTransfers("MDI", date) } returns emptyList()
       every { confirmedArrivalRepository.findAllByArrivalDateAndPrisonNumber(any(), any()) } returns emptyList()
     }

     @Test
     fun `finds match from candidates`() {
       val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

       every { basmService.getArrivals("MDI", date, date) } returns listOf(move)
       every { prisonerSearchService.getCandidateMatches(move) } returns listOf(
         result(move.prisonNumber, move.pncNumber)
       )

       val movement = arrivalsService.getMovements("MDI", date).first()

       assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(move.prisonNumber, move.pncNumber))
     }

     @Test
     fun `Doesn't find match from candidates due to prison number mismatch`() {

       val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

       every { basmService.getArrivals("MDI", date, date) } returns listOf(move)
       every { prisonerSearchService.getCandidateMatches(move) } returns listOf(
         result(ANOTHER_PRISON_NUMBER, move.pncNumber)
       )

       val movement = arrivalsService.getMovements("MDI", date).first()

       assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(null, move.pncNumber))
     }

     @Test
     fun `Doesn't find match from candidates due to PNC mismatch`() {
       val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

       every { basmService.getArrivals("MDI", date, date) } returns listOf(move)
       every { prisonerSearchService.getCandidateMatches(move) } returns listOf(
         result(move.prisonNumber, ANOTHER_PNC_NUMBER)
       )

       val movement = arrivalsService.getMovements("MDI", date).first()

       assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(null, movement.pncNumber))
     }

     @Test
     fun `Doesn't find match as no candidates for new prisoner`() {
       val move = basmOnlyArrival.copy(prisonNumber = null, pncNumber = PNC_NUMBER)

       every { basmService.getArrivals("MDI", date, date) } returns listOf(move)
       every { prisonerSearchService.getCandidateMatches(move) } returns emptyList()

       val movement = arrivalsService.getMovements("MDI", date).first()

       assertThat(movement).extracting("prisonNumber", "pncNumber").isEqualTo(listOf(null, movement.pncNumber))
     }

     @Test
     fun `Doesn't find match as no candidates when prison number provided`() {
       val move = basmOnlyArrival.copy(prisonNumber = PRISON_NUMBER, pncNumber = PNC_NUMBER)

       every { basmService.getArrivals("MDI", date, date) } returns listOf(move)
       every { prisonerSearchService.getCandidateMatches(move) } returns emptyList()

       val movement = arrivalsService.getMovements("MDI", date).first()

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
    private val date = LocalDate.of(2021, 1, 2)

    private const val PRISON_NUMBER = "A1234AA"
    private const val ANOTHER_PRISON_NUMBER = "A1234BB"
    private const val PNC_NUMBER = "99/123456J"
    private const val ANOTHER_PNC_NUMBER = "11/123456J"

    private const val moveId = "beae6404-de16-406f-844a-7e043960d9ec"
    private const val offenderNo = "A1111AA"
    private const val firstName = "Eric"
    private const val lastName = "Bloodaxe"
    private val dateOfBirth: LocalDate = LocalDate.of(1961, 4, 1)
    private const val gender = "M"
    private const val prisonId = "NMI"

    private val basmOnlyArrival = Arrival(
      id = "1",
      firstName = "JIM",
      lastName = "SMITH",
      dateOfBirth = LocalDate.of(1991, 7, 31),
      prisonNumber = "A1234AA",
      pncNumber = "99/123456J",
      date = date,
      fromLocation = "MDI",
      fromLocationType = LocationType.CUSTODY_SUITE,
      isCurrentPrisoner = true
    )

    private val arrivalKnownToNomis = basmOnlyArrival.copy(
      id = "2",
      firstName = "First",
      lastName = "Last",
      dateOfBirth = LocalDate.of(1980, 2, 23),
      prisonNumber = "A1278AA",
      pncNumber = "1234/1234589A",
      fromLocationType = LocationType.PRISON,
      isCurrentPrisoner = true
    )

    val prototypeArrival = Arrival(
      id = moveId,
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = dateOfBirth,
      prisonNumber = null,
      pncNumber = null,
      date = LocalDate.now(),
      fromLocation = "Kingston-upon-Hull Crown Court",
      fromLocationType = LocationType.COURT,
      isCurrentPrisoner = false
    )

    val admitArrivalDetail = AdmitArrivalDetail(
      firstName = firstName,
      lastName = lastName,
      dateOfBirth = dateOfBirth,
      gender = gender,
      prisonId = prisonId,
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03"
    )
  }
}
