package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival

@Service
class PrisonService(
  @Autowired private val prisonApiClient: PrisonApiClient,
  @Autowired private val prisonRegisterClient: PrisonRegisterClient,
) {

  fun getPrisonerImage(prisonNumber: String): ByteArray? = prisonApiClient.getPrisonerImage(prisonNumber)

  fun getPrison(prisonId: String): Prison =
    prisonRegisterClient.getPrison(prisonId) ?: throw NotFoundException("Could not find prison with id: '$prisonId'")

  fun getUserCaseLoads(): List<UserCaseLoad> =
    prisonApiClient.getUserCaseLoads()

  fun getTemporaryAbsences(agencyId: String): List<TemporaryAbsence> =
    prisonApiClient.getTemporaryAbsences(agencyId)

  fun admitOffenderOnNewBooking(
    prisonNumber: String,
    confirmArrivalDetail: ConfirmArrivalDetail
  ): Long =
    prisonApiClient.admitOffenderOnNewBooking(
      prisonNumber,
      with(confirmArrivalDetail) {
        AdmitOnNewBookingDetail(
          prisonId = prisonId!!,
          bookingInTime,
          fromLocationId,
          movementReasonCode = movementReasonCode!!,
          youthOffender,
          cellLocation,
          imprisonmentStatus = imprisonmentStatus!!
        )
      }
    ).bookingId

  fun recallOffender(
    prisonNumber: String,
    confirmArrivalDetail: ConfirmArrivalDetail
  ): Long =
    prisonApiClient.recallOffender(
      prisonNumber,
      with(confirmArrivalDetail) {
        RecallBooking(
          prisonId = prisonId!!,
          bookingInTime,
          fromLocationId,
          movementReasonCode = movementReasonCode!!,
          youthOffender,
          cellLocation,
          imprisonmentStatus = imprisonmentStatus!!
        )
      }
    ).bookingId

  fun createOffender(confirmArrivalDetail: ConfirmArrivalDetail): String =
    prisonApiClient
      .createOffender(
        with(confirmArrivalDetail) {
          CreateOffenderDetail(
            firstName = firstName!!,
            middleName1,
            middleName2,
            lastName = lastName!!,
            dateOfBirth = dateOfBirth!!,
            gender = gender!!,
            ethnicity,
            croNumber,
            pncNumber,
            suffix,
            title
          )
        }
      )
      .offenderNo

  companion object {
    private val numbers = '0'..'9'
    private val letters = 'A'..'Z'

    private operator fun CharRange.invoke(n: Int) = this.shuffled().take(n).joinToString("")
    private fun randomPrisonNumber() = letters(1) + numbers(4) + letters(2)
  }

  fun transferInFromCourt(confirmArrivalDetail: ConfirmArrivalDetail, arrival: Arrival): Long {

    return prisonApiClient.courtTransferIn(
      arrival.prisonNumber!!,
      with(confirmArrivalDetail) {
        CourtTransferIn(
          prisonId!!,
          movementReasonCode,
          commentText,
          bookingInTime
        )
      }
    ).bookingId
  }
}
