package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.search

class Weights {
  companion object {
    const val EXACT_MATCH = 10
    const val PARTIAL_MATCH = 5
    const val FUZZY_CLOSE_MATCH = 3
    const val FUZZY_SLIGHT_MATCH = 2
    const val CONSTANT = -1
    val NO_MATCH = null
  }
}
