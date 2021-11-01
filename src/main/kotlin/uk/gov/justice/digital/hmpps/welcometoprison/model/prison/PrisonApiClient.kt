package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.welcometoprison.model.typeReference
import java.time.LocalDate
import java.time.LocalDateTime

data class CreateOffenderDetail(
  val firstName: String,
  val middleName1: String? = null,
  val middleName2: String? = null,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val gender: String,
  val ethnicity: String? = null,
  val croNumber: String? = null,
  val pncNumber: String? = null,
  val suffix: String? = null,
  val title: String? = null
)

data class AdmitOnNewBookingDetail(
  // prisonId = agency id, eg "MDI"
  val prisonId: String,
  val bookingInTime: LocalDateTime? = null,
  val fromLocationId: String? = null,
  val movementReasonCode: String,
  val youthOffender: Boolean = false,
  val cellLocation: String? = null,
  val imprisonmentStatus: String
)

/**
 * The response has many more fields and nested values, but only offenderNo is of interest
 */
data class ConfirmArrivalResponse(val offenderNo: String)

/*
 * The response has many more fields and nested values but currently only bookingId is needed.
 */
data class InmateDetail(val bookingId: Long)

data class Prison(@JsonProperty("longDescription") val description: String)

@Service
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {
  fun getPrisonerImage(offenderNumber: String): ByteArray? {
    return webClient.get()
      .uri("/api/bookings/offenderNo/$offenderNumber/image/data?fullSizeImage=false")
      .retrieve()
      .bodyToMono(ByteArray::class.java).block()
  }

  fun getAgency(agencyId: String): Prison? {
    return webClient.get()
      .uri("/api/agencies/$agencyId")
      .retrieve()
      .bodyToMono(typeReference<Prison>())
      .onErrorResume(WebClientResponseException::class.java) { emptyWhenNotFound(it) }
      .block()
  }

  fun getPrisonTransfersEnRoute(agencyId: String): List<OffenderMovement> {
    return webClient.get()
      .uri("/api/movements/$agencyId/enroute")
      .retrieve()
      .bodyToMono(typeReference<List<OffenderMovement>>())
      .block() ?: emptyList()
  }

  /**
   * The prison-api end-point expects requests to have role 'BOOKING_CREATE' and scope 'write'.
   */
  fun createOffender(detail: CreateOffenderDetail): ConfirmArrivalResponse =
    webClient.post()
      .uri("/api/offenders")
      .bodyValue(detail)
      .retrieve()
      .bodyToMono(ConfirmArrivalResponse::class.java)
      .block() ?: throw RuntimeException()

  /**
   * The prison-api end-point expects requests to have role 'BOOKING_CREATE', scope 'write' and a (NOMIS) username.
   */
  fun admitOffenderOnNewBooking(offenderNo: String, detail: AdmitOnNewBookingDetail): InmateDetail =
    webClient.post()
      .uri("/api/offenders/$offenderNo/booking")
      .bodyValue(detail)
      .retrieve()
      .bodyToMono(InmateDetail::class.java)
      .block() ?: throw RuntimeException()

  fun <T> emptyWhenNotFound(exception: WebClientResponseException): Mono<T> = emptyWhen(exception, HttpStatus.NOT_FOUND)
  fun <T> emptyWhen(exception: WebClientResponseException, statusCode: HttpStatus): Mono<T> =
    if (exception.statusCode == statusCode) Mono.empty() else Mono.error(exception)
}
