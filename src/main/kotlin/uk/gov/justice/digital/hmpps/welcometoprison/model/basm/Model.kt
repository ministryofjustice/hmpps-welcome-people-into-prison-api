package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalDate

class Model {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class Location(
    val id: String,
    val location_type: String,
    val title: String,
    val nomis_agency_id: String?,
  )

  data class Movement(
    val id: String,
    val type: String,
    val additional_information: String?,
    val date: LocalDate,
    val date_from: LocalDate?,
    val date_to: LocalDate?,
    val move_type: MoveType,
    val reference: String?,
    val status: String,
    val time_due: String?,
    val created_at: String?,
    val updated_at: String?,
    val from_location: Location,
    val to_location: Location,
    val profile: Profile?,
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class Profile(
    val id: String,
    val person: People?,
  )

  @JsonInclude(JsonInclude.Include.NON_NULL)
  data class People(
    val id: String,
    val first_names: String,
    val last_name: String,
    val date_of_birth: LocalDate?,
    val prison_number: String?,
    val criminal_records_office: String?,
    val police_national_computer: String?,
    val gender: Gender?,
  )

  enum class Gender {
    MALE, FEMALE, TRANS;

    companion object {
      @JvmStatic
      @JsonCreator
      fun create(value: JsonNode?) = when (value?.get("key")?.asText()) {
        "male" -> MALE
        "female" -> FEMALE
        "trans" -> TRANS
        else -> null
      }
    }
  }

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
}
