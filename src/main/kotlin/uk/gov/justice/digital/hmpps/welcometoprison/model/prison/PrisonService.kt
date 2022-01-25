package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest

@Service
class PrisonService(
  @Autowired private val prisonApiClient: PrisonApiClient,
  @Autowired private val prisonRegisterClient: PrisonRegisterClient
) {

  fun getPrisonerImage(prisonNumber: String): ByteArray? = prisonApiClient.getPrisonerImage(prisonNumber)

  fun getPrison(prisonId: String): Prison =
    prisonRegisterClient.getPrison(prisonId) ?: throw NotFoundException("Could not find prison with id: '$prisonId'")

  fun getUserCaseLoads(): List<UserCaseLoad> =
    prisonApiClient.getUserCaseLoads()

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

  fun returnFromCourt(confirmCourtReturnRequest: ConfirmCourtReturnRequest, arrival: Arrival): Long {

    return prisonApiClient.courtTransferIn(
      arrival.prisonNumber!!,
      with(confirmCourtReturnRequest) {
        CourtTransferIn(
          prisonId!!,
          null,
          null,
          null
        )
      }
    ).bookingId
  }
}
