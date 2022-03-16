package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.Gender
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.LocationType
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MoveType
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name.properCase
import java.time.LocalDate

@Service
class BasmService(private val basmClient: BasmClient) {

  fun getArrival(moveId: String): Arrival = basmClient.getMovement(moveId)?.toArrival()
    ?: throw NotFoundException("Could not find movement with id: '$moveId'")

  fun getArrivals(prisonId: String, fromDate: LocalDate, toDate: LocalDate): List<Arrival> {
    val prison = basmClient.getPrison(prisonId) ?: return emptyList()
    val movements = basmClient.getMovements(prison.id, fromDate, toDate)

    return movements
      .filter {
        it.profile?.person != null &&
          it.move_type != MoveType.PRISON_TRANSFER
      }
      .map { it.toArrival() }
  }

  private fun Model.Movement.toArrival(): Arrival {
    val personData = this.profile?.person!!

    return Arrival(
      id = this.id,
      firstName = properCase(personData.first_names),
      lastName = properCase(personData.last_name),
      dateOfBirth = personData.date_of_birth,
      prisonNumber = personData.prison_number,
      pncNumber = personData.police_national_computer,
      date = this.date,
      fromLocation = this.from_location.title,
      fromLocationId = this.from_location.nomis_agency_id,
      fromLocationType = this.move_type.toLocationType(),
      gender = this.profile.person.gender?.let { Gender.valueOf(it.name) }
    )
  }

  private fun MoveType.toLocationType(): LocationType = when (this) {
    MoveType.PRISON_REMAND -> LocationType.COURT
    MoveType.PRISON_RECALL -> LocationType.CUSTODY_SUITE
    MoveType.VIDEO_REMAND -> LocationType.CUSTODY_SUITE
    MoveType.PRISON_TRANSFER -> LocationType.PRISON
    else -> LocationType.OTHER
  }
}
