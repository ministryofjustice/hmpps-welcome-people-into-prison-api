package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import tools.jackson.databind.json.JsonMapper
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Gender
import java.util.stream.Stream

class ModelTest {

  private val mapper = JsonMapper.builder().build()

  @ParameterizedTest
  @MethodSource("genderKeysAndValues")
  fun `Gender - create`(key: String, value: Gender?) {
    val json = "{\"key\":\"${key}\"}"
    val gender: Gender? = mapper.readValue(json, Gender::class.java)
    assertThat(gender).isEqualTo(value)
  }

  companion object {
    @JvmStatic
    fun genderKeysAndValues(): Stream<Arguments> = Stream.of(
      arguments("male", Gender.MALE),
      arguments("female", Gender.FEMALE),
      arguments("trans", Gender.TRANS),
      arguments("somethingelse", null),
    )
  }
}
