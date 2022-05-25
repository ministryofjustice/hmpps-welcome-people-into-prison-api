package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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
    @ParameterizedTest(name = "not eligible for fuzzy scoring: {0}")
    @ValueSource(strings = ["a", "ab"])
    fun `not eligible fuzzy matching`(term: String) {
      assertThat(scorer.score(term, term)).isEqualTo(NO_MATCH)
    }
  }

  @Nested
  inner class `Medium Length Words (3-5 characters)` {
    @ParameterizedTest(name = "exact matches are not eligible for fuzzy scoring: {0}")
    @ValueSource(strings = ["abc", "abcd", "abcde"])
    fun `exact matches are not eligible for scoring`(term: String) {
      assertThat(scorer.score(term, term)).isEqualTo(NO_MATCH)
    }

    @ParameterizedTest(name = "searches on three letter fields can have an extra character but not one less: {0}")
    @ValueSource(strings = ["abc"])
    fun `searches on three letter fields can have an extra character but not one less`(term: String) {
      assertThat(scorer.score(term.drop(1), term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(term.dropLast(1), term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score("a$term", term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("${term}a", term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(term.replace('b', 'c'), term)).isEqualTo(FUZZY_CLOSE_MATCH)
    }

    @ParameterizedTest(name = "4-5 letter terms can be one character out: {0}")
    @ValueSource(strings = ["abcd", "abcde"])
    fun `medium length words can be one character out`(term: String) {
      assertThat(scorer.score(term.drop(1), term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(term.dropLast(1), term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("a$term", term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("${term}a", term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(term.replace('b', 'c'), term)).isEqualTo(FUZZY_CLOSE_MATCH)
    }

    @ParameterizedTest(name = "4-5 letter terms cannot be more than one character out: {0}")
    @ValueSource(strings = ["abc", "abcd", "abcde"])
    fun `medium length words cannot be matched if different by distance of 2`(term: String) {
      assertThat(scorer.score(term.drop(2), term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(term.dropLast(2), term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(term.replace('b', 'c').replace('a', 'b'), term)).isEqualTo(NO_MATCH)
    }
  }

  @Nested
  inner class `Long Words (Greater than 5 characters)` {
    @ParameterizedTest(name = "exact matches are not eligible for fuzzy scoring: {0}")
    @ValueSource(strings = ["abcde", "abcdef", "abcdefg"])
    fun `exact matches are not eligible for scoring`(term: String) {
      assertThat(scorer.score(term, term)).isEqualTo(NO_MATCH)
    }

    @ParameterizedTest(name = "one character out: {0}")
    @ValueSource(strings = ["abcdef", "abcdefg", "abcdefgh"])
    fun `one character out`(term: String) {
      assertThat(scorer.score(term.drop(1), term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(term.dropLast(1), term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("a$term", term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score("${term}a", term)).isEqualTo(FUZZY_CLOSE_MATCH)
      assertThat(scorer.score(term.replace('b', 'c'), term)).isEqualTo(FUZZY_CLOSE_MATCH)
    }

    @ParameterizedTest(name = "two characters out: {0}")
    @ValueSource(strings = ["abcdefgh", "abcdefghi", "abcdefghij"])
    fun `two characters out`(term: String) {
      assertThat(scorer.score(term.drop(2), term)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score(term.dropLast(2), term)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score("a${term}b", term)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score("${term}aa", term)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score("bb$term", term)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score("bb$term", term)).isEqualTo(FUZZY_SLIGHT_MATCH)
      assertThat(scorer.score(term.replace('b', 'c').replace('a', 'b'), term)).isEqualTo(FUZZY_SLIGHT_MATCH)
    }

    @ParameterizedTest(name = "three characters out: {0}")
    @ValueSource(strings = ["abcdef", "abcdefg", "abcdefgh"])
    fun `three characters out`(term: String) {
      assertThat(scorer.score(term.drop(3), term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(term.dropLast(3), term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score("a${term}bb", term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score("${term}aaa", term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score("bbb$term", term)).isEqualTo(NO_MATCH)
      assertThat(scorer.score(term.replace('c', 'd').replace('b', 'c').replace('e', 'f'), term)).isEqualTo(NO_MATCH)
    }
  }
}
