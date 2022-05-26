package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.RecentArrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Searcher.Result
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Searcher.SearchStrategy
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.CONSTANT
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.EXACT_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.PARTIAL_MATCH

class SearchByNameAndPrisonNumber : SearchStrategy<String, RecentArrival> {
  private val fuzzyScorer = FuzzyScorer()

  override fun evaluate(query: String, item: RecentArrival) = Result(item, calculateRelevance(query, item))

  private fun calculateRelevance(query: String, item: RecentArrival): Int? {
    val fields = item.splitToFields()
    val terms = query.splitToTerms()
    return when {
      terms.isEmpty() -> CONSTANT
      else -> calculateRelevanceForTerms(terms, fields)
    }
  }

  private fun calculateRelevanceForTerms(terms: List<String>, fields: List<String>) = cartesianProduct(terms, fields)
    .map { (term, field) -> this.calculateRelevanceForField(term, field) }
    .filterNotNull()
    .reduceOrNull { total, relevance -> relevance + total }

  private fun calculateRelevanceForField(term: String, field: String): Int? = when {
    field == term -> EXACT_MATCH
    field.contains(term) -> PARTIAL_MATCH
    else -> fuzzyScorer.score(term, field)
  }
}

private fun String.splitToTerms() =
  this.trim().replace("[,.-]".toRegex(), " ").lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }

private fun RecentArrival.splitToFields() = listOf(prisonNumber, firstName, lastName).map { it.trim().lowercase() }

/**
 * Produces a list that contains every combination of elements in ts and us
 */
private fun <T, U> cartesianProduct(ts: List<T>, us: List<U>): List<Pair<T, U>> =
  ts.flatMap { t -> us.map { u -> t to u } }
