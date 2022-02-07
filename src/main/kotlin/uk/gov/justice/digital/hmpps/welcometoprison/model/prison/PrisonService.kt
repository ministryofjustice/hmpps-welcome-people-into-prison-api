package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnRequest
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns.ConfirmCourtReturnResponse

@Service
class PrisonService(
  @Autowired private val prisonApiClient: PrisonApiClient,
  @Autowired private val prisonRegisterClient: PrisonRegisterClient,
  @Autowired private val locationFormatter: LocationFormatter

) {

  fun getPrisonerImage(prisonNumber: String): ByteArray? = prisonApiClient.getPrisonerImage(prisonNumber)

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
          bookingInTime,
          fromLocationId,
          movementReasonCode = movementReasonCode!!,
          youthOffender,
          cellLocation,
          imprisonmentStatus = imprisonmentStatus!!
        )
      }
    )

  fun recallOffender(
    prisonNumber: String,
    confirmArrivalDetail: ConfirmArrivalDetail
  ): InmateDetail =
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
    )

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

  fun returnFromCourt(
    confirmCourtReturnRequest: ConfirmCourtReturnRequest,
    arrival: Arrival
  ): ConfirmCourtReturnResponse {

    var inmateDetail = prisonApiClient.courtTransferIn(
      arrival.prisonNumber!!,
      with(confirmCourtReturnRequest) {
        CourtTransferIn(prisonId!!)
      }
    )
    return ConfirmCourtReturnResponse(
      prisonNumber = inmateDetail.offenderNo,
      location = locationFormatter.format(inmateDetail.assignedLivingUnit.description),
      bookingId = inmateDetail.bookingId
    )
  }
}
