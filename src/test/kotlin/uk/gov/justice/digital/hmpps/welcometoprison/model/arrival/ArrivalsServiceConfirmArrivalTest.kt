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
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ConfirmArrivalDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.INACTIVE_OUT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

class ArrivalsServiceConfirmArrivalTest {
  private val prisonService: PrisonService = mockk()
  private val basmService: BasmService = mockk()
  private val prisonerSearchService: PrisonerSearchService = mockk()
  private val confirmedArrivalService: ConfirmedArrivalService = mockk()

  private val arrivalsService =
    ArrivalsService(basmService, prisonService, prisonerSearchService, confirmedArrivalService)
  val result = { prisonNumber: String?, pnc: String? -> MatchPrisonerResponse(prisonNumber, pnc, "ACTIVE IN") }

  @Test
  fun `Confirm arrival not known to NOMIS`() {

    every { basmService.getArrival(any()) } returns prototypeArrival.copy()
    every { prisonerSearchService.getCandidateMatches(any()) } returns emptyList()
    every { prisonService.createOffender(any()) } returns OFFENDER_NO
    every { prisonService.admitOffenderOnNewBooking(any(), any()) } returns Unit
    every { confirmedArrivalService.add(any(), any(), any(), any(), any(), any()) } returns Unit

    val response = arrivalsService.confirmArrival(MOVE_ID, confirmArrivalDetail)

    assertThat(response.offenderNo).isEqualTo(OFFENDER_NO)

    verify { basmService.getArrival(MOVE_ID) }
    verifySequence {
      prisonService.createOffender(confirmArrivalDetail)
      prisonService.admitOffenderOnNewBooking(OFFENDER_NO, confirmArrivalDetail)
    }
    verify {
      confirmedArrivalService.add(
        MOVE_ID,
        OFFENDER_NO,
        PRISON_ID,
        0,
        LocalDate.now(),
        ArrivalType.NEW_TO_PRISON
      )
    }
  }

  @Test
  fun `Confirm arrival matched to NOMIS offender who is not in custody`() {

    every { basmService.getArrival(any()) } returns prototypeArrival.copy(
      prisonNumber = OFFENDER_NO,
      isCurrentPrisoner = false
    )
    every { prisonerSearchService.getCandidateMatches(any()) } returns listOf(
      MatchPrisonerResponse(prisonerNumber = OFFENDER_NO, pncNumber = null, status = INACTIVE_OUT)
    )
    every { prisonService.admitOffenderOnNewBooking(any(), any()) } returns Unit
    every { confirmedArrivalService.add(any(), any(), any(), any(), any(), any()) } returns Unit

    val response = arrivalsService.confirmArrival(MOVE_ID, confirmArrivalDetail)

    assertThat(response.offenderNo).isEqualTo(OFFENDER_NO)

    verify { basmService.getArrival(MOVE_ID) }
    verify { prisonService.admitOffenderOnNewBooking(OFFENDER_NO, confirmArrivalDetail) }
    verify {
      confirmedArrivalService.add(
        MOVE_ID, OFFENDER_NO, PRISON_ID,
        0, LocalDate.now(), ArrivalType.NEW_BOOKING_EXISTING_OFFENDER
      )
    }
  }

  @Test
  fun `Confirm arrival matched to NOMIS offender who is in custody`() {

    every { basmService.getArrival(any()) } returns prototypeArrival.copy(prisonNumber = OFFENDER_NO)
    every { prisonerSearchService.getCandidateMatches(any()) } returns listOf(
      MatchPrisonerResponse(prisonerNumber = OFFENDER_NO, pncNumber = null, status = "ACTIVE IN")
    )

    Assertions.assertThatThrownBy {
      arrivalsService.confirmArrival(MOVE_ID, confirmArrivalDetail)
    }.isInstanceOf(IllegalArgumentException::class.java)

    verify { basmService.getArrival(MOVE_ID) }
  }

  companion object {
    private const val MOVE_ID = "beae6404-de16-406f-844a-7e043960d9ec"
    private const val OFFENDER_NO = "A1111AA"
    private const val FIRST_NAME = "Eric"
    private const val LAST_NAME = "Bloodaxe"
    private val DOB: LocalDate = LocalDate.of(1961, 4, 1)
    private const val GENDER = "M"
    private const val PRISON_ID = "NMI"

    val prototypeArrival = Arrival(
      id = MOVE_ID,
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DOB,
      prisonNumber = null,
      pncNumber = null,
      date = LocalDate.now(),
      fromLocation = "Kingston-upon-Hull Crown Court",
      fromLocationType = LocationType.COURT,
      isCurrentPrisoner = false
    )

    val confirmArrivalDetail = ConfirmArrivalDetail(
      firstName = FIRST_NAME,
      lastName = LAST_NAME,
      dateOfBirth = DOB,
      gender = GENDER,
      prisonId = PRISON_ID,
      movementReasonCode = "N",
      imprisonmentStatus = "SENT03"
    )
  }
}
