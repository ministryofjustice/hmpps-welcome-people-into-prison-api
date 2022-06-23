package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ArrivalType.TEMPORARY_ABSENCE
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsencesArrival

@Service
@Transactional
class TemporaryAbsenceService(
  private val prisonApiClient: PrisonApiClient,
  private val locationFormatter: LocationFormatter,
  private val arrivalListener: ArrivalListener,
) {

  fun getTemporaryAbsence(agencyId: String, prisonNumber: String): TemporaryAbsenceResponse =
    getTemporaryAbsences(agencyId).find { it.prisonNumber == prisonNumber }
      ?: throw NotFoundException("Could not find temporary absence with prisonNumber: '$prisonNumber'")

  fun getTemporaryAbsences(agencyId: String) = prisonApiClient.getTemporaryAbsences(agencyId).map {
    TemporaryAbsenceResponse(
      firstName = Name.properCase(it.firstName),
      lastName = Name.properCase(it.lastName),
      dateOfBirth = it.dateOfBirth,
      prisonNumber = it.offenderNo,
      reasonForAbsence = it.movementReason,
      movementDateTime = it.movementTime
    )
  }

  fun confirmTemporaryAbsencesArrival(
    prisonNumber: String,
    request: ConfirmTemporaryAbsenceRequest
  ): ConfirmTemporaryAbsenceResponse {
    val inmateDetail = prisonApiClient.confirmTemporaryAbsencesArrival(prisonNumber, request.toArrival())
    arrivalListener.arrived(
      ArrivalEvent(
        prisonId = inmateDetail.agencyId,
        prisonNumber = inmateDetail.offenderNo,
        bookingId = inmateDetail.bookingId,
        arrivalType = TEMPORARY_ABSENCE,
      )
    )
    return ConfirmTemporaryAbsenceResponse(prisonNumber, locationFormatter.extract(inmateDetail))
  }

  private fun ConfirmTemporaryAbsenceRequest.toArrival() = TemporaryAbsencesArrival(
    agencyId, movementReasonCode, commentText, receiveTime
  )
}
