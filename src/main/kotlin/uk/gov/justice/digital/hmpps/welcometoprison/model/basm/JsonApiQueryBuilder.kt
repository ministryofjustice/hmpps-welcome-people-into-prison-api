package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

typealias Param<T> = Pair<String, T>

object JsonApiQueryBuilder {
  enum class Order { ASC, DESC }

  fun `query of`(
    includes: List<String>? = null,
    filters: Map<String, List<String>>? = null,
    sort: Pair<String, Order>? = null,
    page: Int? = null,
    perPage: Int? = null,
  ): String {
    val includeParams = includes.toParams { listOf("include" to it.joinToString(",")) }
    val perPageParam = perPage.toParams { listOf("per_page" to it) }
    val pageParam = page.toParams { listOf("page" to it) }
    val sortParam = sort.toParams { (name, order) -> listOf("sort[by]" to name, "sort[direction]" to order) }
    val filterParams =
      filters.toParams { it.toList().map { (name, values) -> "filter[$name]" to values.joinToString(",") } }

    val params = arrayOf(includeParams, filterParams, pageParam, perPageParam, sortParam).flatten()
    return if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
  }

  private fun <T, R> T?.toParams(build: (value: T) -> List<Param<R>>) =
    if (this == null) emptyArray() else build(this).map { it.write() }.toTypedArray()

  private fun <T> Param<T>.write() = "$first=$second"
}
