package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnResponse

@Service
class PrisonService(
  private val prisonApiClient: PrisonApiClient,
  private val prisonRegisterClient: PrisonRegisterClient,
  private val locationFormatter: LocationFormatter
) {

  fun getPrisonerImage(prisonNumber: String): ByteArray? =
    prisonApiClient.getPrisonerImage(prisonNumber)
      ?: throw NotFoundException("Could not find image for: '$prisonNumber'")

  fun getPrison(prisonId: String): Prison =
    prisonRegisterClient.getPrison(prisonId) ?: throw NotFoundException("Could not find prison with id: '$prisonId'")

  fun getUserCaseLoads(): List<UserCaseLoad> =
    prisonApiClient.getUserCaseLoads()

  fun admitOffenderOnNewBooking(
    prisonNumber: String,
    confirmArrivalDetail: ConfirmArrivalDetail
  ): InmateDetail =
    prisonApiClient.admitOffenderOnNewBooking(
      prisonNumber,
      with(confirmArrivalDetail) {
        AdmitOnNewBookingDetail(
          prisonId = prisonId!!,
          fromLocationId = fromLocationId,
          movementReasonCode = movementReasonCode!!,
          youthOffender = youthOffender,
          imprisonmentStatus = imprisonmentStatus!!
        )
      }
    )

  fun recallOffender(prisonNumber: String, detail: ConfirmArrivalDetail): InmateDetail =
    prisonApiClient.recallOffender(
      prisonNumber,
      with(detail) {
        RecallBooking(
          prisonId = prisonId!!,
          fromLocationId = fromLocationId,
          movementReasonCode = movementReasonCode!!,
          youthOffender = youthOffender,
          imprisonmentStatus = imprisonmentStatus!!
        )
      }
    )

  fun createOffender(confirmArrivalDetail: ConfirmArrivalDetail): String =
    prisonApiClient
      .createOffender(
        with(confirmArrivalDetail) {
          CreateOffenderDetail(
            firstName = firstName!!,
            lastName = lastName!!,
            dateOfBirth = dateOfBirth!!,
            gender = sex!!,
            pncNumber = pncNumber,
          )
        }
      )
      .offenderNo

  fun returnFromCourt(prisonId: String, prisonNumber: String): ConfirmCourtReturnResponse {
    val inmateDetail = prisonApiClient.courtTransferIn(prisonNumber, CourtTransferIn(prisonId))
    return ConfirmCourtReturnResponse(
      prisonNumber = inmateDetail.offenderNo,
      location = locationFormatter.extract(inmateDetail),
      bookingId = inmateDetail.bookingId
    )
  }
}
