package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.EXACT_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.FUZZY_CLOSE_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.FUZZY_SLIGHT_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.NO_MATCH

class FuzzyScorerTest {

  private val scorer = FuzzyScorer()

  @Test
  fun `term is empty`() {
    assertThat(scorer.score("", "field")).isEqualTo(NO_MATCH)
  }

  @Test
  fun `query is empty`() {
    assertThat(scorer.score("term", "")).isEqualTo(NO_MATCH)
  }

  @Nested
  inner class `Short Words (1 -2 characters)` {
    @ParameterizedTest(name = "exact matches: {0}")
    @ValueSource(strings = ["a", "ab"])
    fun `exact matches`(value: String) {
      assertThat(scorer.score(value, value)).isEqualTo(EXACT_MATCH)
    }
  }

  @Nested
  inner class `Medium Length Words (3-5 characters)` {
    @ParameterizedTest(name = "exact matches: {0}")
    @ValueSource(strings = ["abc", "abcd", "abcde"])
    fun `exact matches are not eligible for scoring`(value: String) {
      assertThat(scorer.score(value, value)).isEqualTo(EXACT_MATCH)
    }

    @ParameterizedTest(name = "searches on three letter fields can have an extra character but not one less: {0}")
    @ValueSource(strings = ["abc"])
    fun `searches on three letter fields can have an extra character but not one less`(value: String) {
      assertThat(scorer.score(value.drop(1), value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(value.dropLast(1), value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score("a$value", value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("${value}a", value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(value.replace('b', 'c'), value)).isEqualTo(FUZZY_CLOSE_MATCH)
    }

    @ParameterizedTest(name = "4-5 letter values can be one character out: {0}")
    @ValueSource(strings = ["abcd", "abcde"])
    fun `medium length words can be one character out`(value: String) {
      assertThat(scorer.score(value.drop(1), value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(value.dropLast(1), value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("a$value", value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("${value}a", value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(value.replace('b', 'c'), value)).isEqualTo(FUZZY_CLOSE_MATCH)
    }

    @ParameterizedTest(name = "4-5 letter values cannot be more than one character out: {0}")
    @ValueSource(strings = ["abc", "abcd", "abcde"])
    fun `medium length words cannot be matched if different by distance of 2`(value: String) {
      assertThat(scorer.score(value.drop(2), value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(value.dropLast(2), value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(value.replace('b', 'c').replace('a', 'b'), value)).isEqualTo(NO_MATCH)
    }
  }

  @Nested
  inner class `Long Words (Greater than 5 characters)` {
    @ParameterizedTest(name = "exact matches: {0}")
    @ValueSource(strings = ["abcde", "abcdef", "abcdefg"])
    fun `exact matches`(value: String) {
      assertThat(scorer.score(value, value)).isEqualTo(EXACT_MATCH)
    }

    @ParameterizedTest(name = "one character out: {0}")
    @ValueSource(strings = ["abcdef", "abcdefg", "abcdefgh"])
    fun `one character out`(value: String) {
      assertThat(scorer.score(value.drop(1), value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(value.dropLast(1), value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("a$value", value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("${value}a", value)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(value.replace('b', 'c'), value)).isEqualTo(FUZZY_CLOSE_MATCH)
    }

    @ParameterizedTest(name = "two characters out: {0}")
    @ValueSource(strings = ["abcdefgh", "abcdefghi", "abcdefghij"])
    fun `two characters out`(value: String) {
      assertThat(scorer.score(value.drop(2), value)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score(value.dropLast(2), value)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score("a${value}b", value)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score("${value}aa", value)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score("bb$value", value)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score("bb$value", value)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score(value.replace('b', 'c').replace('a', 'b'), value)).isEqualTo(FUZZY_SLIGHT_MATCH)
    }

    @ParameterizedTest(name = "three characters out: {0}")
    @ValueSource(strings = ["abcdef", "abcdefg", "abcdefgh"])
    fun `three characters out`(value: String) {
      assertThat(scorer.score(value.drop(3), value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(value.dropLast(3), value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score("a${value}bb", value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score("${value}aaa", value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score("bbb$value", value)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(value.replace('c', 'd').replace('b', 'c').replace('e', 'f'), value)).isEqualTo(NO_MATCH)
    }
  }
}
