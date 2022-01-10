package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsencesArrival

@Service
@Transactional
class TemporaryAbsenceService(
  private val prisonApiClient: PrisonApiClient,
) {

  fun getTemporaryAbsence(agencyId: String, prisonNumber: String): TemporaryAbsence =
    getTemporaryAbsences(agencyId).find { it.prisonNumber == prisonNumber }
      ?: throw NotFoundException("Could not find temporary absence with prisonNumber: '$prisonNumber'")

  fun getTemporaryAbsences(agencyId: String): List<TemporaryAbsence> {
    val temporaryAbsences = prisonApiClient.getTemporaryAbsences(agencyId)

    return temporaryAbsences.map {
      TemporaryAbsence(
        firstName = Name.properCase(it.firstName),
        lastName = Name.properCase(it.lastName),
        dateOfBirth = it.dateOfBirth,
        prisonNumber = it.prisonNumber,
        reasonForAbsence = it.reasonForAbsence,
      )
    }
  }

  fun temporaryAbsencesArrival(
    prisonNumber: String,
    temporaryAbsencesArrival: TemporaryAbsencesArrival
  ): InmateDetail {
    return prisonApiClient.temporaryAbsencesArrival(
      prisonNumber,
      with(temporaryAbsencesArrival) {
        TemporaryAbsencesArrival(
          agencyId,
          movementReasonCode,
          commentText,
          receiveTime
        )
      }
    )
  }
}
