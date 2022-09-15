package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.config.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType.TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
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

  fun getTransfer(agencyId: String, prisonNumber: String): Transfer =
    getTransfers(agencyId).find { it.prisonNumber == prisonNumber }
      ?: throw NotFoundException("Could not find transfer with prisonNumber: '$prisonNumber'")

  fun getTransfers(agencyId: String): List<Transfer> {
    val transfers = prisonApiClient.getPrisonTransfersEnRoute(agencyId, LocalDate.now())
    val pncNumbers = prisonerSearchService.getPncNumbers(transfers.map { it.offenderNo })

    return transfers.map {
      Transfer(
        firstName = Name.properCase(it.firstName),
        lastName = Name.properCase(it.lastName),
        dateOfBirth = it.dateOfBirth,
        fromLocation = it.fromAgencyDescription,
        prisonNumber = it.offenderNo,
        date = it.movementDate,
        pncNumber = pncNumbers[it.offenderNo],
      )
    }
  }

  fun transferInOffender(
    prisonNumber: String,
    transferInDetail: TransferInDetail,
    movementId: String? = null
  ): TransferResponse {
    val inmateDetail = prisonApiClient.transferIn(prisonNumber, transferInDetail.toArrival())
    arrivalListener.arrived(
      ArrivalEvent(
        movementId = movementId,
        prisonId = transferInDetail.prisonId,
        prisonNumber = inmateDetail.offenderNo,
        bookingId = inmateDetail.bookingId,
        arrivalType = TRANSFER,
      )
    )
    return TransferResponse(prisonNumber = inmateDetail.offenderNo, location = locationFormatter.extract(inmateDetail))
  }

  private fun TransferInDetail.toArrival() = TransferIn(cellLocation, commentText, receiveTime)
}
