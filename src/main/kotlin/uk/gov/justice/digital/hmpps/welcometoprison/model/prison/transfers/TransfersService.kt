package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TransferIn
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchService

@Service
@Transactional
class TransfersService(
  private val prisonApiClient: PrisonApiClient,
  private val prisonerSearchService: PrisonerSearchService,
  private val locationFormatter: LocationFormatter,
) {

  fun getTransfer(agencyId: String, prisonNumber: String): Transfer =
    getTransfers(agencyId).find { it.prisonNumber == prisonNumber }
      ?: throw NotFoundException("Could not find transfer with prisonNumber: '$prisonNumber'")

  fun getTransfers(agencyId: String): List<Transfer> {
    val transfers = prisonApiClient.getPrisonTransfersEnRoute(agencyId)
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
    transferInDetail: TransferInDetail
  ): TransferResponse {

    val inmateDetail = prisonApiClient.transferIn(
      prisonNumber,
      with(transferInDetail) {
        TransferIn(
          cellLocation,
          commentText,
          receiveTime
        )
      }
    )
    val livingUnitName = inmateDetail.assignedLivingUnit?.description
      ?: throw IllegalArgumentException("Prisoner: '$prisonNumber' do not have assigned living unit")
    return TransferResponse(
      prisonNumber = inmateDetail.offenderNo,
      location = locationFormatter.format(livingUnitName)
    )
  }
}
