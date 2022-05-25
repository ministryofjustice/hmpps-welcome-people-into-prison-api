package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.RecentArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Searcher.Result
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Searcher.SearchStrategy
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.CONSTANT
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.EXACT_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.FUZZY_CLOSE_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.FUZZY_SLIGHT_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.NO_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.PARTIAL_MATCH
import java.time.LocalDate

class SearchByNameAndPrisonNumberTest {

  private val strategy: SearchStrategy<String, RecentArrival> = SearchByNameAndPrisonNumber()

  @Test
  fun `default relevance when empty query`() {
    val results = strategy.evaluate("", item("A1234AA", "Jim", "Smith"))
    assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), CONSTANT))
  }

  @Test
  fun `no match`() {
    val results = strategy.evaluate("Wilson", item("A1234AA", "Jim", "Smith"))
    assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), NO_MATCH))
  }

  @Nested
  inner class `Exact matches` {
    @Test
    fun `exact match on one field`() {
      val results = strategy.evaluate("A1234AA", item("A1234AA", "Jim", "Smith"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), EXACT_MATCH))
    }

    @Test
    fun `exact match on all fields`() {
      val results = strategy.evaluate("A1234AA Jim Smith", item("A1234AA", "Jim", "Smith"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), EXACT_MATCH * 3))
    }

    @Test
    fun `order doesn't matter`() {
      val results = strategy.evaluate("Smith Jim A1234AA", item("A1234AA", "Jim", "Smith"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), EXACT_MATCH * 3))
    }

    @Test
    fun `exact match on all fields with commas`() {
      val results = strategy.evaluate("A1234AA Smith, Jim", item("A1234AA", "Jim", "Smith"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), EXACT_MATCH * 3))
    }

    @Test
    fun `exact match on all fields with fullstops`() {
      val results = strategy.evaluate("A1234AA. Smith. Jim", item("A1234AA", "Jim", "Smith"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), EXACT_MATCH * 3))
    }

    @Test
    fun `one term exact match on multiple fields`() {
      val results = strategy.evaluate("Jim", item("A1234AA", "Jim", "Jim"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Jim"), EXACT_MATCH * 2))
    }
  }

  @Nested
  inner class `Partial match` {
    @ParameterizedTest(name = "partial match on one field: {0}")
    @ValueSource(strings = ["smi", "mit", "ith", "mith", "A123", "Ji"])
    fun `partial match on one field`(term: String) {
      val results = strategy.evaluate(term, item("A1234AA", "Jim", "Smith"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), PARTIAL_MATCH))
    }

    @Test
    fun `term contains field`() {
      val results = strategy.evaluate("Smithson", item("A1234AA", "Jim", "Smith"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smith"), NO_MATCH))
    }

    @Test
    fun `one term partial match on multiple fields`() {
      val results = strategy.evaluate("im", item("A1234AA", "Jim", "Jims"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Jims"), PARTIAL_MATCH * 2))
    }
  }

  @Nested
  inner class `Fuzzy match` {
    @ParameterizedTest(name = "fuzzy match with close match (distance 1): {0}")
    @ValueSource(strings = ["a1234ab", "smithsun"])
    fun `fuzzy match with close match (distance 1)`(term: String) {
      val results = strategy.evaluate(term, item("A1234AA", "Jim", "Smithson"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smithson"), FUZZY_CLOSE_MATCH))
    }

    @ParameterizedTest(name = "fuzzy match with slight match (distance 2): {0}")
    @ValueSource(strings = ["A1234BB", "smathsun"])
    fun `fuzzy match with slight match (distance 2)`(term: String) {
      val results = strategy.evaluate(term, item("A1234AA", "Jim", "Smithson"))
      assertThat(results).isEqualTo(Result(item("A1234AA", "Jim", "Smithson"), FUZZY_SLIGHT_MATCH))
    }
  }

  fun item(prisonNumber: String, firstName: String, lastName: String) = RecentArrival(
    prisonNumber = prisonNumber,
    dateOfBirth = LocalDate.ofEpochDay(1),
    firstName = firstName,
    lastName = lastName,
    movementDateTime = LocalDate.ofEpochDay(1).atStartOfDay(),
    location = null
  )
}
