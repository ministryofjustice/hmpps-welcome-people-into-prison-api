package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.JsonApiQueryBuilder.Order.asc
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.JsonApiQueryBuilder.`query of`
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Location
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.deserializer.JsonApiResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

@Service
class BasmClient(@Qualifier("basmApiWebClient") private val webClient: WebClient) {

  fun getPrison(prisonId: String): Location? = get(
    path = "/api/reference/locations",
    query = `query of`(filters = mapOf("nomis_agency_id" to listOf(prisonId))),
    type = object : ParameterizedTypeReference<JsonApiResponse<Location>>() {}
  ).payload[0]

  fun getMovements(prisonUuid: String, from: LocalDate, to: LocalDate): List<Movement> = get(
    path = "/api/moves",
    query = `query of`(
      filters = mapOf(
        "to_location_id" to listOf(prisonUuid),
        "date_from" to listOf(from.format(ISO_DATE)),
        "date_to" to listOf(to.format(ISO_DATE)),
        "status" to listOf("requested", "accepted", "booked", "in_transit", "completed"),
      ),
      sort = "date" to asc,
      page = 1,
      perPage = 200,
      includes = listOf("profile.person", "from_location", "to_location")
    ),
    type = object : ParameterizedTypeReference<JsonApiResponse<Movement>>() {}
  ).payload

  fun getMovement(moveId: String): Movement? = get(
    path = "/api/moves/$moveId",
    query = `query of`(
      includes = listOf("profile.person", "from_location", "to_location")
    ),
    type = object : ParameterizedTypeReference<JsonApiResponse<Movement>>() {}
  ).payload.firstOrNull()

  private fun <T : Any> get(path: String, query: String = "", type: ParameterizedTypeReference<T>) = webClient.get()
    .uri("$path$query")
    .header("Accept", "application/vnd.api+json; version=2")
    .retrieve()
    .bodyToMono(type)
    .block()!!
}
