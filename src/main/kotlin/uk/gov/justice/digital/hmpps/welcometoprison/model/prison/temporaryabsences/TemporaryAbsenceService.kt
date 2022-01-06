package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonApiClient

@Service
@Transactional
class TemporaryAbsenceService(
  private val prisonApiClient: PrisonApiClient,
) {

  fun getTemporaryAbsence(agencyId: String, prisonNumber: String): TemporaryAbsence =
    getTemporaryAbsences(agencyId).find { it.prisonNumber == prisonNumber }
      ?: throw NotFoundException("Could not find temporary absence with prisonNumber: '$prisonNumber'")

  fun getTemporaryAbsences(agencyId: String): List<TemporaryAbsence> =
    prisonApiClient.getTemporaryAbsences(agencyId)
}
