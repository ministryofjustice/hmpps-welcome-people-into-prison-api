package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

class Searcher<Q, T : Comparable<T>>(private val strategy: Strategy<Q, T>) {

  fun search(query: Q?, input: List<T>): List<T> =
    searchWithRelevance(query, input).map { it.item }

  internal fun searchWithRelevance(query: Q?, input: List<T>): List<Result<T>> {
    val results = query
      ?.let { input.map { strategy.evaluate(query, it) }.filter { it.relevance != null } }
      ?: input.map { Result(it, 1) }

    return results.sortedWith(mostRelevantThenNatural)
  }

  private val mostRelevantThenNatural =
    compareBy<Result<T>> { -it.relevance!! }.thenBy { it.item }

  data class Result<T : Comparable<T>>(val item: T, val relevance: Int?)

  interface Strategy<Q, T : Comparable<T>> {
    fun evaluate(query: Q, item: T): Result<T>
  }
}
