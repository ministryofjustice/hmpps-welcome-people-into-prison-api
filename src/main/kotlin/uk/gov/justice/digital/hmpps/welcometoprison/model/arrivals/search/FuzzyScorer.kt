package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

import org.apache.commons.text.similarity.LevenshteinDistance
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.EXACT_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.FUZZY_CLOSE_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.FUZZY_SLIGHT_MATCH
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search.Weights.Companion.NO_MATCH

class FuzzyScorer {
  fun score(term: String, field: String): Int? {
    if (field.isBlank() || term.isBlank()) {
      return NO_MATCH
    }
    return when (term distanceTo field) {
      -1 -> NO_MATCH
      0 -> EXACT_MATCH
      1 -> FUZZY_CLOSE_MATCH
      else -> FUZZY_SLIGHT_MATCH
    }
  }

  private infix fun String.distanceTo(field: String): Int {
    // Short terms are less likely to have typos
    val allowedDistance = when {
      this.length < 3 -> 0
      this.length < 6 -> 1
      else -> 2
    }
    return LevenshteinDistance(allowedDistance).apply(this, field)
  }
}
