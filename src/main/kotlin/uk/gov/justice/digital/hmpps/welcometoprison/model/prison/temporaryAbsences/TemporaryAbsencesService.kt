package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryAbsences

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.InmateDetail
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.TemporaryAbsencesArrival

@Service
@Transactional
class TemporaryAbsencesService(
  private val prisonApiClient: PrisonApiClient

) {
  fun temporaryAbsencesArrival (
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
