package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.LocationType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Includes
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Location
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MoveType
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.People
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Profile
import java.time.LocalDate
import kotlin.reflect.KClass

@Service
class BasmService(private val basmClient: BasmClient) {

  fun <T : Includes> Map<String, Map<String, Includes>>.getAs(key: String, type: KClass<T>): (id: String) -> T? {
    val includesOfType = this[key] ?: emptyMap()
    return { id ->
      val item = includesOfType[id]
      check(item == null || type.isInstance(item)) { "item expected to be $type, was ${item?.javaClass}" }
      @Suppress("UNCHECKED_CAST")
      item as T?
    }
  }

  fun getMoves(prisonId: String, fromDate: LocalDate, toDate: LocalDate): List<Movement> {

    val prison = basmClient.getPrison(prisonId) ?: return emptyList()

    val movementData = basmClient.getMovements(prison.getId(), fromDate, toDate)

    val includes = movementData.included.groupBy { it.type }.mapValues { (_, v) -> v.associateBy { it.getId() } }
    val profiles = includes.getAs(Profile.TYPE, Profile::class)
    val people = includes.getAs(People.TYPE, People::class)
    val locations = includes.getAs(Location.TYPE, Location::class)

    return movementData.data
      .filter {
        it.relationships.profile.data?.id != null &&
          it.relationships.from_location.data?.id != null &&
          it.relationships.to_location.data?.id != null &&
          profiles(it.relationships.profile.data.id)?.relationships?.person?.data?.id != null &&
          it.attributes.move_type != MoveType.PRISON_TRANSFER
      }
      .map {
        val fromLocationUuid = it.relationships.from_location.data?.id
        val profileId = it.relationships.profile.data?.id!!
        val personId = profiles(profileId)?.relationships?.person?.data?.id!!
        val personData = people(personId)?.attributes!!

        Movement(
          id = it.id,
          firstName = personData.first_names!!,
          lastName = personData.last_name!!,
          dateOfBirth = personData.date_of_birth!!,
          prisonNumber = personData.prison_number,
          pncNumber = personData.police_national_computer,
          date = it.attributes.date!!,
          fromLocation = locations(fromLocationUuid!!)?.attributes?.title!!,
          fromLocationType = mapBasmMoveTypeToLocationType(it.attributes.move_type)
        )
      }
  }

  private fun mapBasmMoveTypeToLocationType(moveType: MoveType): LocationType = when (moveType) {
    MoveType.PRISON_REMAND -> LocationType.COURT
    MoveType.PRISON_RECALL -> LocationType.CUSTODY_SUITE
    MoveType.VIDEO_REMAND -> LocationType.CUSTODY_SUITE
    MoveType.PRISON_TRANSFER -> LocationType.PRISON
    else -> LocationType.OTHER
  }
}
