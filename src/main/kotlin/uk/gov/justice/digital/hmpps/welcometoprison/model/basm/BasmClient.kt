package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.config.typeReference
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.JsonApiQueryBuilder.Order.ASC
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.JsonApiQueryBuilder.`queryOf`
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Location
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.deserializer.JsonApiResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter.ISO_DATE

@Service
class BasmClient(@Qualifier("basmApiWebClient") private val webClient: WebClient) {

  fun getPrison(prisonId: String): Location? = get(
    path = "/api/reference/locations",
    query = `queryOf`(filters = mapOf("nomis_agency_id" to listOf(prisonId))),
    type = typeReference<JsonApiResponse<Location>>(),
  ).block()?.firstOrNull()

  fun getMovements(prisonUuid: String, from: LocalDate, to: LocalDate): List<Movement> = get(
    path = "/api/moves",
    query = `queryOf`(
      filters = mapOf(
        "to_location_id" to listOf(prisonUuid),
        "date_from" to listOf(from.format(ISO_DATE)),
        "date_to" to listOf(to.format(ISO_DATE)),
        "status" to listOf("requested", "accepted", "booked", "in_transit", "completed"),
      ),
      sort = "date" to ASC,
      page = 1,
      perPage = 200,
      includes = listOf("profile.person", "from_location", "to_location", "profile.person.gender"),
    ),
    type = typeReference<JsonApiResponse<Movement>>(),
  ).block() ?: emptyList()

  fun getMovement(moveId: String): Movement? = get(
    path = "/api/moves/$moveId",
    query = `queryOf`(
      includes = listOf("profile.person", "from_location", "to_location", "profile.person.gender", "profile.person_escort_record.responses", "profile.person_escort_record.responses.question"),
    ),
    type = typeReference<JsonApiResponse<Movement>>(),
  ).onErrorResume(WebClientResponseException::class.java) { emptyWhenNotFound(it) }
    .block()?.firstOrNull()

  private fun <T : Any> get(
    path: String,
    query: String = "",
    type: ParameterizedTypeReference<JsonApiResponse<T>>,
  ): Mono<List<T>> =
    webClient.get()
      .uri("$path$query")
      .header("Accept", "application/vnd.api+json; version=2")
      .header("X-Current-User", SecurityContextHolder.getContext().authentication.principal.toString())
      .retrieve()
      .bodyToMono(type)
      .map { it.payload }

  fun <T> emptyWhenNotFound(exception: WebClientResponseException): Mono<T> = emptyWhen(exception, NOT_FOUND)
  fun <T> emptyWhen(exception: WebClientResponseException, statusCode: HttpStatus): Mono<T> =
    if (exception.statusCode == statusCode) Mono.empty() else Mono.error(exception)
}
