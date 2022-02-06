package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsencesArrival

@Service
@Transactional
class TemporaryAbsenceService(
  private val prisonApiClient: PrisonApiClient,
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
    offenderNo: String,
    confirmTemporaryAbsenceRequest: ConfirmTemporaryAbsenceRequest
  ): ConfirmTemporaryAbsenceResponse {
    val inmateDetail = prisonApiClient.confirmTemporaryAbsencesArrival(
      offenderNo,
      with(confirmTemporaryAbsenceRequest) {
        TemporaryAbsencesArrival(
          agencyId,
          movementReasonCode,
          commentText,
          receiveTime
        )
      }
    )
    return with(inmateDetail) {
      ConfirmTemporaryAbsenceResponse(
        prisonerNumber = offenderNo,
        location = assignedLivingUnit.description
      )
    }
  }
}
