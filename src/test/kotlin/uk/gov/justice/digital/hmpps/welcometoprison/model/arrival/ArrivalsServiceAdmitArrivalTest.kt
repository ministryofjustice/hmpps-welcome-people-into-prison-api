package uk.gov.justice.digital.hmpps.welcometoprison.model.arrival

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalType
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrivalService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.AdmitArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

class ArrivalsServiceAdmitArrivalTest {
  private val prisonService: PrisonService = mockk()
  private val basmService: BasmService = mockk()
  private val prisonerSearchService: PrisonerSearchService = mockk()
  private val confirmedArrivalService: ConfirmedArrivalService = mockk()

  private val arrivalsService =
    ArrivalsService(basmService, prisonService, prisonerSearchService, confirmedArrivalService)
  val result = { prisonNumber: String?, pnc: String? -> MatchPrisonerResponse(prisonNumber, pnc, "ACTIVE IN") }

    @Test
    fun `Admit arrival not known to NOMIS`() {

      every { basmService.getArrival(any()) } returns prototypeArrival.copy()
      every { prisonerSearchService.getCandidateMatches(any()) } returns emptyList()
      every { prisonService.createOffender(any()) } returns offenderNo
      every { prisonService.admitOffenderOnNewBooking(any(), any()) } returns Unit
      every { confirmedArrivalService.add(any(), any(), any(), any(), any(), any()) } returns Unit

      val response = arrivalsService.admitArrival(moveId, admitArrivalDetail)

      assertThat(response.offenderNo).isEqualTo(offenderNo)

      verify { basmService.getArrival(moveId) }
      verifySequence {
        prisonService.createOffender(admitArrivalDetail)
        prisonService.admitOffenderOnNewBooking(offenderNo, admitArrivalDetail)
      }
      verify {
        confirmedArrivalService.add(
          moveId,
          offenderNo,
          prisonId,
          0,
          LocalDate.now(),
          ArrivalType.NEW_TO_PRISON
        )
      }
    }

    @Test
    fun `Admit arrival matched to NOMIS offender who is not in custody`() {

      every { basmService.getArrival(any()) } returns prototypeArrival.copy(
        prisonNumber = offenderNo,
        isCurrentPrisoner = false
      )
      every { prisonerSearchService.getCandidateMatches(any()) } returns listOf(
        MatchPrisonerResponse(prisonerNumber = offenderNo, pncNumber = null, status = INACTIVE_OUT)
      )
      every { prisonService.admitOffenderOnNewBooking(any(), any()) } returns Unit
      every { confirmedArrivalService.add(any(), any(), any(), any(), any(), any()) } returns Unit

      val response = arrivalsService.admitArrival(moveId, admitArrivalDetail)

      assertThat(response.offenderNo).isEqualTo(offenderNo)

      verify { basmService.getArrival(moveId) }
      verify { prisonService.admitOffenderOnNewBooking(offenderNo, admitArrivalDetail) }
      verify {
        confirmedArrivalService.add(
          moveId, offenderNo, prisonId,
          0, LocalDate.now(), ArrivalType.NEW_BOOKING_EXISTING_OFFENDER
        )
      }
    }

    @Test
    fun `Admit arrival matched to NOMIS offender who is in custody`() {

      every { basmService.getArrival(any()) } returns prototypeArrival.copy(prisonNumber = offenderNo)
      every { prisonerSearchService.getCandidateMatches(any()) } returns listOf(
        MatchPrisonerResponse(prisonerNumber = offenderNo, pncNumber = null, status = "ACTIVE IN")
      )

      Assertions.assertThatThrownBy {
        arrivalsService.admitArrival(moveId, admitArrivalDetail)
      }.isInstanceOf(IllegalArgumentException::class.java)

      verify { basmService.getArrival(moveId) }
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
