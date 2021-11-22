package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException

@Service
@Transactional
class TransfersService(
  private val prisonApiClient: PrisonApiClient,
) {

  fun getTransfer(agencyId: String, prisonNumber: String): Transfer =
    getTransfers(agencyId).find { it.prisonNumber == prisonNumber }
      ?: throw NotFoundException("Could not find transfer with prisonNumber: '$prisonNumber'")

  fun getTransfers(agencyId: String): List<Transfer> =
    prisonApiClient.getPrisonTransfersEnRoute(agencyId).map {
      Transfer(
        firstName = Name.properCase(it.firstName),
        lastName = Name.properCase(it.lastName),
        dateOfBirth = it.dateOfBirth,
        fromLocation = it.fromAgencyDescription,
        prisonNumber = it.offenderNo,
        date = it.movementDate,
        pncNumber = null,
      )
    }

  fun transferInOffender(
    prisonNumber: String,
    transferInDetail: TransferInDetail
  ) {
    prisonApiClient.transferIn(
      prisonNumber,
      with(transferInDetail) {
        TransferIn(
          cellLocation,
          commentText,
          receiveTime
        )
      }
    )
  }
}
