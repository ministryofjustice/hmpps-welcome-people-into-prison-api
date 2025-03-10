package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.RecentArrival
import java.time.LocalDate

class SearchScenariosTest {

  private val searcher = Searcher(SearchByNameAndPrisonNumber())

  @Test
  fun `scenario 1`() {
    verifySearch(
      "John",
      listOf(
        ExpectedResult("John", "Johns", 15),
        ExpectedResult("John", "Johnson", 15),
        ExpectedResult("John", "Night", 10),
        ExpectedResult("Jane", "Johnson", 5),
        ExpectedResult("Simon", "Johnson", 5),
        ExpectedResult("Jon", "Smith", 3),
        ExpectedResult("kohn", "Smith", 3),
        ExpectedIrrelevant("Jane", "Smith"),
        ExpectedIrrelevant("Simon", "Marshall"),
      ),
    )
  }

  @Test
  fun `scenario 2`() {
    verifySearch(
      "John Smith",
      listOf(
        ExpectedResult("John", "Smith", 20),
        ExpectedResult("John", "Johns", 15),
        ExpectedResult("John", "Johnson", 15),
        ExpectedResult("Jon", "Smith", 13),
        ExpectedResult("kohn", "Smith", 13),
        ExpectedResult("John", "Night", 10),
        ExpectedResult("Jane", "Smith", 10),
        ExpectedResult("Jane", "Johnson", 5),
        ExpectedResult("Simon", "Johnson", 5),
        ExpectedIrrelevant("Simon", "Marshall"),
      ),
    )
  }

  @Test
  fun `scenario 3`() {
    verifySearch(
      "o",
      listOf(
        ExpectedResult("John", "Johns", 10),
        ExpectedResult("John", "Johnson", 10),
        ExpectedResult("Simon", "Johnson", 10),
        ExpectedResult("Jane", "Johnson", 5),
        ExpectedResult("Simon", "Marshall", 5),
        ExpectedResult("John", "Night", 5),
        ExpectedResult("John", "Smith", 5),
        ExpectedResult("Jon", "Smith", 5),
        ExpectedResult("kohn", "Smith", 5),
        ExpectedIrrelevant("Jane", "Smith"),
      ),
    )
  }

  fun verifySearch(query: String, expected: List<Result>) {
    val input = expected.map { it.toRecentArrival() }.shuffled()
    val expectedResults = expected.filterIsInstance<ExpectedResult>().map { it.toSearchResult() }
    val results = searcher.searchWithRelevance(query, input)
    assertThat(results).containsExactly(*expectedResults.toTypedArray())
  }

  sealed class Result(open val firstName: String, open val lastName: String) {
    fun toRecentArrival() = RecentArrival(
      prisonNumber = "A1234AA",
      dateOfBirth = LocalDate.ofEpochDay(1),
      firstName = firstName,
      lastName = lastName,
      movementDateTime = LocalDate.ofEpochDay(1).atStartOfDay(),
      location = null,
    )
  }

  data class ExpectedResult(override val firstName: String, override val lastName: String, val score: Int) : Result(firstName, lastName) {
    fun toSearchResult() = Searcher.Result(this.toRecentArrival(), score)
  }

  data class ExpectedIrrelevant(override val firstName: String, override val lastName: String) : Result(firstName, lastName)
}
