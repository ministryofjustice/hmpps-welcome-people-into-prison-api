package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.LocationType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MoveType
import java.time.LocalDate

@Service
class BasmService(private val basmClient: BasmClient) {

  fun getMovement(moveId: String): Movement {
    val movement = basmClient.getMovement(moveId)
    return toMovement(movement!!)
  }

  fun getMoves(prisonId: String, fromDate: LocalDate, toDate: LocalDate): List<Movement> {
    val prison = basmClient.getPrison(prisonId) ?: return emptyList()
    val movements = basmClient.getMovements(prison.id, fromDate, toDate)

    return movements
      .filter {
        it.profile?.person != null &&
          it.move_type != MoveType.PRISON_TRANSFER
      }
      .map { toMovement(it) }
  }

  private fun toMovement(it: Model.Movement): Movement {
    val personData = it.profile?.person!!

    return Movement(
      id = it.id,
      firstName = personData.first_names!!,
      lastName = personData.last_name!!,
      dateOfBirth = personData.date_of_birth!!,
      prisonNumber = personData.prison_number,
      pncNumber = personData.police_national_computer,
      date = it.date!!,
      fromLocation = it.from_location.title!!,
      fromLocationType = mapBasmMoveTypeToLocationType(it.move_type)
    )
  }

  private fun mapBasmMoveTypeToLocationType(moveType: MoveType): LocationType = when (moveType) {
    MoveType.PRISON_REMAND -> LocationType.COURT
    MoveType.PRISON_RECALL -> LocationType.CUSTODY_SUITE
    MoveType.VIDEO_REMAND -> LocationType.CUSTODY_SUITE
    MoveType.PRISON_TRANSFER -> LocationType.PRISON
    else -> LocationType.OTHER
  }
}
