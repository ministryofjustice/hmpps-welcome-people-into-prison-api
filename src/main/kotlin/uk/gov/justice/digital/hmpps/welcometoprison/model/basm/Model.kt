package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.time.LocalDate

class Model {
  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class PrisonResponseWrapper(
    val data: List<Location>,
    val meta: ResponseMetadata
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class MovementResponseWrapper(
    val data: List<Movement>,
    val meta: ResponseMetadata?,
    val included: List<Includes>
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class ResponseMetadata(
    val pagination: ResponsePagination
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class ResponsePagination(
    val per_page: Int,
    val total_pages: Int,
    val total_objects: Int
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class Movement(
    val id: String,
    val type: String,
    val attributes: MovementAttributes,
    val relationships: Relationships
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class MovementAttributes(
    val additional_information: String?,
    val date: LocalDate?,
    val date_from: LocalDate?,
    val date_to: LocalDate?,
    val move_type: MoveType,
    val reference: String?,
    val status: String,
    val time_due: String?,
    val created_at: String?,
    val updated_at: String?
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")
  @JsonSubTypes(
    JsonSubTypes.Type(value = People::class, name = People.TYPE),
    JsonSubTypes.Type(value = Location::class, name = Location.TYPE),
    JsonSubTypes.Type(value = Profile::class, name = Profile.TYPE)
  )
  abstract class Includes(val type: String) {
    abstract fun getId(): String
  }

  data class Profile(
    private val id: String,
    val relationships: IncludesRelationships?
  ) : Includes(TYPE) {
    override fun getId() = this.id
    companion object {
      const val TYPE = "profiles"
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class IncludesRelationships(
    val person: Relationship?
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class Location(
    private val id: String,
    val attributes: LocationAttributes
  ) : Includes(TYPE) {
    override fun getId() = id

    companion object {
      const val TYPE = "locations"
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class LocationAttributes(
    val key: String,
    val title: String,
    val location_type: String,
    val nomis_agency_id: String?
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class People(
    private val id: String,
    val attributes: PeopleAttributes?,
    val relationships: IncludesRelationships?
  ) : Includes(TYPE) {
    override fun getId() = id

    companion object {
      const val TYPE = "people"
    }
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class PeopleAttributes(
    val first_names: String?,
    val last_name: String?,
    val date_of_birth: LocalDate?,
    val prison_number: String?,
    val criminal_records_office: String?,
    val police_national_computer: String?
  )

  enum class MoveType {
    @JsonProperty("prison_remand")
    PRISON_REMAND,

    @JsonProperty("court_appearance")
    COURT_APPEARANCE,

    @JsonProperty("prison_recall")
    PRISON_RECALL,

    @JsonProperty("video_remand")
    VIDEO_REMAND,

    @JsonProperty("prison_transfer")
    PRISON_TRANSFER,
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class Relationships(
    val from_location: Relationship,
    val to_location: Relationship,
    val profile: Relationship,
    val supplier: Relationship?,
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class Relationship(
    val data: RelationshipData?
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class RelationshipData(
    val id: String?,
    val type: String?
  )
}
