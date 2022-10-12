package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.config.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.formatter.LocationFormatter
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalEvent
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ArrivalListener
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalType.TEMPORARY_ABSENCE
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsencesArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.PrisonerSearchApiClient

@Service
@Transactional
class TemporaryAbsenceService(
  private val prisonApiClient: PrisonApiClient,
  private val locationFormatter: LocationFormatter,
  private val arrivalListener: ArrivalListener,
  private val prisonerSearchApiClient: PrisonerSearchApiClient
) {

  fun getTemporaryAbsence(prisonNumber: String): TemporaryAbsenceResponse {
    val prisoner = prisonerSearchApiClient.getPrisoner(prisonNumber)
      ?: throw NotFoundException("Could not find prisoner with prisonNumber: '$prisonNumber'")
    return getTemporaryAbsences(prisoner.prisonId!!).find { it.prisonNumber == prisonNumber }
      ?: throw NotFoundException("Could not find temporary absence with prisonNumber: '$prisonNumber'")
  }

  fun getTemporaryAbsences(prisonId: String) = prisonApiClient.getTemporaryAbsences(prisonId).map {
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
    request: ConfirmTemporaryAbsenceRequest,
  ): ConfirmTemporaryAbsenceResponse {
    val inmateDetail = prisonApiClient.confirmTemporaryAbsencesArrival(prisonNumber, request.toArrival())
    arrivalListener.arrived(
      ArrivalEvent(
        arrivalId = request.arrivalId,
        prisonId = request.prisonId,
        prisonNumber = inmateDetail.offenderNo,
        bookingId = inmateDetail.bookingId,
        arrivalType = TEMPORARY_ABSENCE,
      )
    )
    return ConfirmTemporaryAbsenceResponse(prisonNumber, locationFormatter.extract(inmateDetail))
  }

  private fun ConfirmTemporaryAbsenceRequest.toArrival() = TemporaryAbsencesArrival(
    prisonId, movementReasonCode, commentText, receiveTime
  )
}
