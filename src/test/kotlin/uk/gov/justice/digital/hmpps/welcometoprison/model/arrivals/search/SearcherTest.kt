package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Searcher.Result
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Searcher.SearchStrategy
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.CONSTANT

class SearcherTest {

  // All numbers <= query are returned, numbers further away from query are more relevant
  private val lessThanAndEqualStrategy = object : SearchStrategy<Int, Int> {
    override fun evaluate(query: Int, item: Int): Result<Int> {
      return if (item <= query) {
        Result(item, query - item)
      } else {
        Result(item, null)
      }
    }
  }

  val searcher = Searcher(lessThanAndEqualStrategy)

  @Test
  fun `Returns empty when no results and no query`() {
    val results = searcher.searchWithRelevance(null, emptyList())
    assertThat(results).isEmpty()
  }

  @Test
  fun `Returns empty when no results and query`() {
    val results = searcher.searchWithRelevance(1, emptyList())
    assertThat(results).isEmpty()
  }

  @Test
  fun `Returns all results when no search term provided`() {
    val items = listOf(1, 2)
    val results = searcher.searchWithRelevance(null, items)
    assertThat(results).containsExactly(Result(1, relevance = CONSTANT), Result(2, relevance = CONSTANT))
  }

  @Test
  fun `Filters out irrelevant items`() {
    val items = listOf(6, 2, 3, 1, 4)
    val results = searcher.searchWithRelevance(3, items)
    assertThat(results).containsExactlyInAnyOrder(
      Result(2, relevance = 1),
      Result(3, relevance = 0),
      Result(1, relevance = 2),
    )
  }

  @Test
  fun `sort returned results by relevance`() {
    val items = listOf(6, 2, 3, 1, 4)
    val results = searcher.searchWithRelevance(3, items)
    // In this case relevance is determined by distance of result
    assertThat(results).containsExactly(
      Result(1, relevance = 2),
      Result(2, relevance = 1),
      Result(3, relevance = 0),
    )
  }

  @Test
  fun `handles duplicate results with search`() {
    val items = listOf(6, 2, 3, 1, 2, 4)
    val results = searcher.searchWithRelevance(3, items)
    // In this case relevance is determined by distance of result
    assertThat(results).containsExactly(
      Result(1, relevance = 2),
      Result(2, relevance = 1),
      Result(2, relevance = 1),
      Result(3, relevance = 0),
    )
  }

  @Test
  fun `Sorted by natural comparator when equal relevance`() {
    val items = listOf(1, 3, 6, 2)
    val results = searcher.searchWithRelevance(null, items)
    assertThat(results).containsExactly(
      Result(1, relevance = CONSTANT),
      Result(2, relevance = CONSTANT),
      Result(3, relevance = CONSTANT),
      Result(6, relevance = CONSTANT),
    )
  }

  @Test
  fun `Handles sorting`() {
    val items = listOf(1, 2, 3, 6, 2)
    val results = searcher.searchWithRelevance(null, items)
    assertThat(results).containsExactly(
      Result(1, relevance = CONSTANT),
      Result(2, relevance = CONSTANT),
      Result(2, relevance = CONSTANT),
      Result(3, relevance = CONSTANT),
      Result(6, relevance = CONSTANT),
    )
  }

  @Test
  fun `search returns unwrapped items`() {
    val items = listOf(1, 2, 3, 6, 2)
    val results = searcher.search(null, items)
    assertThat(results).containsExactly(
      1,
      2,
      2,
      3,
      6,
    )
  }
}
