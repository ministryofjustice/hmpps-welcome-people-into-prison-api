package uk.gov.justice.digital.hmpps.welcometoprison.model.basm.deserializer

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.utils.loadJson
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.reflect.KClass

class JsonApiDeserializerTest {

  private val mapper = ObjectMapper()
    .registerModule(KotlinModule.Builder().build())
    .registerModule(JavaTimeModule())
    .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)

  private fun wrapper(type: KClass<*>) =
    mapper.typeFactory.constructParametricType(JsonApiResponse::class.java, type.java)

  private fun <T : Any> readJsonApiResponse(type: KClass<T>, fileName: String) =
    mapper.readValue<JsonApiResponse<T>>(fileName.loadJson(this), wrapper(type))

  @Test
  fun `check single empty deserialization`() {
    data class Move(var reference: String?)

    val result = readJsonApiResponse(Move::class, "single-empty")

    assertThat(result.payload).containsExactly(Move(null))
  }

  @Test
  fun `check empty deserialization`() {
    data class Move(var reference: String?)

    val result = readJsonApiResponse(Move::class, "empty")

    assertThat(result.payload).isEmpty()
  }

  @Test
  fun `check simple single deserialization`() {
    data class Move(var reference: String, var created_at: OffsetDateTime)

    val result = readJsonApiResponse(Move::class, "simple-single")

    assertThat(result.payload).containsExactly(
      Move(
        "MUT4738J",
        OffsetDateTime.of(LocalDateTime.of(2021, 9, 29, 8, 5, 9), ZoneOffset.UTC),
      ),
    )
  }

  @Test
  fun `check simple list deserialization`() {
    data class Move(var reference: String, var created_at: OffsetDateTime)

    val result = readJsonApiResponse(Move::class, "simple-list")

    assertThat(result.payload).containsExactly(
      Move(
        "MUT4738J",
        OffsetDateTime.of(LocalDateTime.of(2021, 9, 29, 8, 5, 9), ZoneOffset.UTC),
      ),
    )
  }

  @Test
  fun `check relationships without inclusion deserialization`() {
    data class Person(val type: String, val id: String)
    data class Profile(val type: String, val id: String)
    data class Move(
      var id: String,
      var reference: String,
      var created_at: OffsetDateTime,
      val person: Person?,
      val profile: Profile?,
    )

    val result = readJsonApiResponse(Move::class, "with-non-included-relation")

    assertThat(result.payload).containsExactly(
      Move(
        "476d47a3-013a-4772-94c7-5d043b0d0574",
        "MUT4738J",
        OffsetDateTime.of(LocalDateTime.of(2021, 9, 29, 8, 5, 9), ZoneOffset.UTC),
        Person("people", "bd1bf67e-d160-4032-ad59-8f62cd7b25fe"),
        Profile("profiles", "45a2b4a8-38ec-46f4-b882-148a21ebbe6e"),
      ),
    )
  }

  @Test
  fun `check relationships and inclusions deserialization`() {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Gender(
      val key: String,
      val title: String,
      val description: String?,
      val disabled_at: String?,
      val nomis_code: String?,
    )

    data class Person(
      val type: String,
      val id: String,
      @JsonProperty("first_names") val firstName: String,
      @JsonProperty("last_name") val lastName: String,
      val gender: Gender?,
    )

    data class Location(
      val type: String,
      val id: String,
      @JsonProperty("nomis_agency_id") val agencyId: String,
      val title: String,
    )

    data class Move(
      var id: String,
      var reference: String,
      var created_at: OffsetDateTime,
      val person: Person?,
      @JsonProperty("from_location")
      val fromLocation: Location,
      @JsonProperty("to_location")
      val toLocation: Location,
    )

    val result = readJsonApiResponse(Move::class, "with-included-relations")

    assertThat(result.payload).containsExactly(
      Move(
        "476d47a3-013a-4772-94c7-5d043b0d0574",
        "MUT4738J",
        OffsetDateTime.of(LocalDateTime.of(2021, 9, 29, 8, 5, 9), ZoneOffset.UTC),
        Person(
          "people",
          "bd1bf67e-d160-4032-ad59-8f62cd7b25fe",
          "Alexis",
          "Jones",
          Gender("male", "Male", null, null, "M"),
        ),
        Location("locations", "6c1047cf-c8e8-4034-9899-d05ac1b07038", "PENRCT", "Penrith County Court"),
        Location("locations", "a2bc2abf-75fe-4b7f-bf5a-a755bc290757", "NMI", "NOTTINGHAM (HMP)"),
      ),
    )
  }

  @Test
  fun `handles deserializing null relations`() {
    data class Person(
      val type: String,
      val id: String,
    )

    data class Location(
      val type: String,
      val id: String,
      @JsonProperty("nomis_agency_id") val agencyId: String,
      val title: String,
    )

    data class Move(
      var id: String,
      var reference: String,
      val person: Person?,
    )

    val result = readJsonApiResponse(Move::class, "with-null-relations")

    assertThat(result.payload).containsExactly(
      Move(
        "476d47a3-013a-4772-94c7-5d043b0d0574",
        "MUT4738J",
        null,
      ),
    )
  }
}
