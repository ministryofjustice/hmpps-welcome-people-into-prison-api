package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
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

data class RecallBooking(
  val prisonId: String,
  val recallTime: LocalDateTime? = null,
  val fromLocationId: String? = null,
  val movementReasonCode: String,
  val youthOffender: Boolean = false,
  val cellLocation: String? = null,
  val imprisonmentStatus: String
)

data class TransferIn(
  val cellLocation: String? = null,
  val commentText: String? = null,
  val receiveTime: LocalDateTime? = null
)

/**
 * The response has many more fields and nested values, but only offenderNo is of interest
 */
data class ConfirmArrivalResponse(val offenderNo: String)

/*
 * The response has many more fields and nested values but currently only bookingId is needed.
 */
data class InmateDetail(val bookingId: Long)

fun <T> emptyWhenNotFound(exception: WebClientResponseException): Mono<T> = emptyWhen(exception, HttpStatus.NOT_FOUND)
fun <T> emptyWhen(exception: WebClientResponseException, statusCode: HttpStatus): Mono<T> =
  if (exception.statusCode == statusCode) Mono.empty() else Mono.error(exception)

@Component
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {
  fun getPrisonerImage(offenderNumber: String): ByteArray? =
    webClient.get()
      .uri("/api/bookings/offenderNo/$offenderNumber/image/data?fullSizeImage=false")
      .retrieve()
      .bodyToMono(ByteArray::class.java).block()

  fun getAgency(agencyId: String): Prison? =
    webClient.get()
      .uri("/api/agencies/$agencyId")
      .retrieve()
      .bodyToMono(typeReference<Prison>())
      .onErrorResume(WebClientResponseException::class.java) { emptyWhenNotFound(it) }
      .block()

  fun getPrisonTransfersEnRoute(agencyId: String): List<OffenderMovement> =
    webClient.get()
      .uri("/api/movements/$agencyId/enroute")
      .retrieve()
      .bodyToMono(typeReference<List<OffenderMovement>>())
      .block() ?: emptyList()

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

  /**
   * The prison-api end-point expects requests to have role 'TRANSFER_PRISONER', scope 'write' and a (NOMIS) username.
   */
  fun recallOffender(offenderNo: String, detail: RecallBooking): InmateDetail =
    webClient.put()
      .uri("/api/offenders/$offenderNo/recall")
      .bodyValue(detail)
      .retrieve()
      .bodyToMono(InmateDetail::class.java)
      .block() ?: throw IllegalStateException("No response from prison api")

  /**
   * The prison-api end-point expects requests to have role 'TRANSFER_PRISONER', scope 'write' and a (NOMIS) username.
   */
  fun transferIn(offenderNo: String, detail: TransferIn) =
    webClient.put()
      .uri("/api/offenders/$offenderNo/transfer-in")
      .bodyValue(detail)
      .retrieve()
      .toBodilessEntity()
      .block() ?: throw IllegalStateException("No response from prison api")
}
