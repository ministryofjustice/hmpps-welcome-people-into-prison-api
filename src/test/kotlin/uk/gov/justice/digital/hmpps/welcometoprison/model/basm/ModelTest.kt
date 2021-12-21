package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.ALL_MOVEMENT_REASON_CODES
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.RECALL_MOVEMENT_REASON_CODES
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Gender

class ModelTest {

  private val mapper: ObjectMapper = ObjectMapper().setSerializationInclusion(Include.NON_NULL)

  @ParameterizedTest
  @MethodSource("genderKeysAndValues")
  fun `Gender - create`(key: String, value: Gender?) {
    val json = "{\"key\":\"${key}\"}"
    val gender: Gender? = mapper.readValue(json, Gender::class.java)
    assertThat(gender).isEqualTo(value)
  }

  companion object {
    @JvmStatic
    fun genderKeysAndValues(): Stream<Arguments> =
      Stream.of(
        arguments("male", Gender.MALE),
        arguments("female", Gender.FEMALE),
        arguments("trans", Gender.TRANS),
        arguments("somethingelse", null),
      )
  }
}
