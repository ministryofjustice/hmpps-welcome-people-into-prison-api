package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsencesArrival

@Service
@Transactional
class TemporaryAbsenceService(
  private val prisonApiClient: PrisonApiClient,
  private val locationFormatter: LocationFormatter
) {

  fun getTemporaryAbsence(agencyId: String, prisonNumber: String): TemporaryAbsenceResponse =
    getTemporaryAbsences(agencyId).find { it.prisonNumber == prisonNumber }
      ?: throw NotFoundException("Could not find temporary absence with prisonNumber: '$prisonNumber'")

  fun getTemporaryAbsences(agencyId: String): List<TemporaryAbsenceResponse> {
    val temporaryAbsences = prisonApiClient.getTemporaryAbsences(agencyId)

    return temporaryAbsences.map {
      TemporaryAbsenceResponse(
        firstName = Name.properCase(it.firstName),
        lastName = Name.properCase(it.lastName),
        dateOfBirth = it.dateOfBirth,
        prisonNumber = it.offenderNo,
        reasonForAbsence = it.movementReason,
        movementDateTime = it.movementTime
      )
    }
  }

  fun confirmTemporaryAbsencesArrival(
    prisonNumber: String,
    confirmTemporaryAbsenceRequest: ConfirmTemporaryAbsenceRequest
  ): ConfirmTemporaryAbsenceResponse {
    val inmateDetail = prisonApiClient.confirmTemporaryAbsencesArrival(
      prisonNumber,
      with(confirmTemporaryAbsenceRequest) {
        TemporaryAbsencesArrival(
          agencyId,
          movementReasonCode,
          commentText,
          receiveTime
        )
      }
    )
    val livingUnitName = inmateDetail.assignedLivingUnit?.description
      ?: throw IllegalArgumentException("Prisoner: '$prisonNumber' do not have assigned living unit")
    return with(inmateDetail) {
      ConfirmTemporaryAbsenceResponse(
        prisonNumber = prisonNumber,
        location = locationFormatter.format(livingUnitName)
      )
    }
  }
}
