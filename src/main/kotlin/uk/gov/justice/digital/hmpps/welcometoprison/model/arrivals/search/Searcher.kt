package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

class Searcher<Q, T : Comparable<T>>(private val strategy: SearchStrategy<Q, T>) {

  fun search(query: Q?, input: List<T>): List<T> =
    searchWithRelevance(query, input).map { it.item }

  internal fun searchWithRelevance(query: Q?, items: List<T>): List<Result<T>> {
    val results = query
      ?.let { items.findRelevantResults(query) }
      ?: items.withConstantRelevance()

    return results.sortedWith(mostRelevantThenNatural)
  }

  private fun List<T>.withConstantRelevance() = this.map { Result(it, Weights.CONSTANT) }

  private fun List<T>.findRelevantResults(query: Q) =
    this.map { strategy.evaluate(query, it) }.filter { it.relevance != null }

  private val mostRelevantThenNatural =
    compareBy<Result<T>> { -it.relevance!! }.thenBy { it.item }

  data class Result<T : Comparable<T>>(
    val item: T,
    // null denotes irrelevant
    val relevance: Int?,
  )

  interface SearchStrategy<Q, T : Comparable<T>> {
    fun evaluate(query: Q, item: T): Result<T>
  }
}
