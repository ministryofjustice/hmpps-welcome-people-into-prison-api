package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.config.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType.TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.OffenderMovement
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.OffenderMovementWithPnc
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferIn
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService
import java.time.LocalDate

@Service
@Transactional
class TransfersService(
  private val prisonApiClient: PrisonApiClient,
  private val prisonerSearchService: PrisonerSearchService,
  private val locationFormatter: LocationFormatter,
  private val arrivalListener: ArrivalListener,
) {

  fun getTransfer(agencyId: String, prisonNumber: String): TransferWithMainOffence {
    val offenderMovementWithPnc = getOffenderMovementsWithPnc(agencyId, prisonNumber).first()
    var mainOffence = prisonApiClient.getMainOffence(offenderMovementWithPnc.offenderMovement.bookingId)
    return TransferWithMainOffence(
      firstName = Name.properCase(offenderMovementWithPnc.offenderMovement.firstName),
      lastName = Name.properCase(offenderMovementWithPnc.offenderMovement.lastName),
      dateOfBirth = offenderMovementWithPnc.offenderMovement.dateOfBirth,
      fromLocation = offenderMovementWithPnc.offenderMovement.fromAgencyDescription,
      prisonNumber = offenderMovementWithPnc.offenderMovement.offenderNo,
      date = offenderMovementWithPnc.offenderMovement.movementDate,
      pncNumber = offenderMovementWithPnc.pncNumber,
      mainOffence = mainOffence?.offenceDescription?.lowercase()?.replaceFirstChar { a -> a.uppercase() },
    )
  }

  fun getOffenderMovementsWithPnc(agencyId: String, forPrisonNumber: String?): List<OffenderMovementWithPnc> {
    var transfers: List<OffenderMovement> = prisonApiClient.getPrisonTransfersEnRoute(agencyId, LocalDate.now())
    if (!forPrisonNumber.isNullOrBlank()) {
      transfers = transfers.filter { it.offenderNo == forPrisonNumber }
      if (transfers.isEmpty()) {
        throw NotFoundException("Could not find transfer with prisonNumber: '$forPrisonNumber'")
      }
    }
    val pncNumbers = prisonerSearchService.getPncNumbers(transfers.map { it.offenderNo })
    return transfers.map {
      OffenderMovementWithPnc(
        offenderMovement = it,
        pncNumber = pncNumbers[it.offenderNo],
      )
    }
  }

  fun getTransfers(agencyId: String): List<Transfer> = getOffenderMovementsWithPnc(agencyId, null).map {
    Transfer(
      firstName = Name.properCase(it.offenderMovement.firstName),
      lastName = Name.properCase(it.offenderMovement.lastName),
      dateOfBirth = it.offenderMovement.dateOfBirth,
      fromLocation = it.offenderMovement.fromAgencyDescription,
      prisonNumber = it.offenderMovement.offenderNo,
      date = it.offenderMovement.movementDate,
      pncNumber = it.pncNumber,
    )
  }

  fun transferInOffender(
    prisonNumber: String,
    transferInDetail: TransferInDetail,
  ): TransferResponse {
    val inmateDetail = prisonApiClient.transferIn(prisonNumber, transferInDetail.toArrival())
    arrivalListener.arrived(
      ArrivalEvent(
        arrivalId = transferInDetail.arrivalId,
        prisonId = transferInDetail.prisonId,
        prisonNumber = inmateDetail.offenderNo,
        bookingId = inmateDetail.bookingId,
        arrivalType = TRANSFER,
      ),
    )
    return TransferResponse(prisonNumber = inmateDetail.offenderNo, location = locationFormatter.extract(inmateDetail))
  }

  private fun TransferInDetail.toArrival() = TransferIn(cellLocation, commentText, receiveTime)
}
