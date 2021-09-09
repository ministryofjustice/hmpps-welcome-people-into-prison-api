package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.MoveType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Includes
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Location
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
          profiles(it.relationships.profile.data.id)?.relationships?.person?.data?.id != null
      }
      .map {
        val fromLocationUuid = it.relationships.from_location.data?.id
        val profileId = it.relationships.profile.data?.id!!
        val personId = profiles(profileId)?.relationships?.person?.data?.id!!
        val personData = people(personId)?.attributes!!

        Movement(
          firstName = personData.first_names!!,
          lastName = personData.last_name!!,
          prisonNumber = personData.prison_number,
          pncNumber = personData.police_national_computer!!,
          dateOfBirth = personData.date_of_birth!!,
          date = it.attributes.date!!,
          moveType = MoveType.valueOf(it.attributes.move_type.name),
          fromLocation = locations(fromLocationUuid!!)?.attributes?.title!!
        )
      }
  }
}
