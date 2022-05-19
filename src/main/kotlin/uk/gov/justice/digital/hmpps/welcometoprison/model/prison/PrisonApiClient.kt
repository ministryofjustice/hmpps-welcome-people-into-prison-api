package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.welcometoprison.model.ClientException
import uk.gov.justice.digital.hmpps.welcometoprison.model.typeReference
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

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
  val prisonId: String,
  val fromLocationId: String? = null,
  val movementReasonCode: String,
  val youthOffender: Boolean = false,
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

data class TemporaryAbsencesArrival(
  val agencyId: String? = null,
  val movementReasonCode: String? = null,
  val commentText: String? = null,
  val receiveTime: LocalDateTime? = null
)

data class CourtTransferIn(
  val agencyId: String,
  val movementReasonCode: String? = null,
  val commentText: String? = null,
  val dateTime: LocalDateTime? = null
)

data class TemporaryAbsence(
  val offenderNo: String? = null,
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val movementTime: LocalDateTime? = null,
  val toCity: String? = null,
  val toAgency: String? = null,
  val toAgencyDescription: String? = null,
  val movementReasonCode: String,
  val movementReason: String,
  val commentText: String? = null

)

data class Movement(
  val offenderNo: String,
  val bookingId: Long,
  val dateOfBirth: LocalDate,
  val firstName: String,
  val lastName: String,
  val fromAgencyId: String? = null,
  val fromAgencyDescription: String? = null,
  val toAgencyId: String? = null,
  val toAgencyDescription: String? = null,
  val movementTime: LocalTime,
  val movementDateTime: LocalDateTime,
  val location: String
)

/*
 * The response has many more fields and nested values but currently only bookingId is needed.
 */
data class InmateDetail(
  val bookingId: Long,
  val offenderNo: String,
  val assignedLivingUnit: AssignedLivingUnit? = null
)

data class AssignedLivingUnit(
  val agencyId: String,
  val locationId: Int?,
  val description: String?,
  val agencyName: String
)

data class ErrorResponse(
  val status: Int? = null,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null
)

data class UserCaseLoad(
  val caseLoadId: String,
  val description: String,
)

fun <T> emptyWhenNotFound(exception: WebClientResponseException): Mono<T> = emptyWhen(exception, HttpStatus.NOT_FOUND)
fun <T> emptyWhen(exception: WebClientResponseException, statusCode: HttpStatus): Mono<T> =
  if (exception.statusCode == statusCode) Mono.empty() else Mono.error(exception)

fun propagateClientError(response: ClientResponse, message: String) =
  response.bodyToMono(ErrorResponse::class.java).map {
    ClientException(it, message)
  }

@Component
class PrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {
  fun getPrisonerImage(offenderNumber: String): ByteArray? =
    webClient.get()
      .uri("/api/bookings/offenderNo/$offenderNumber/image/data?fullSizeImage=false")
      .retrieve()
      .bodyToMono(ByteArray::class.java)
      .onErrorResume(WebClientResponseException::class.java) { emptyWhenNotFound(it) }
      .block()

  fun getAgency(agencyId: String): Prison? =
    webClient.get()
      .uri("/api/agencies/$agencyId")
      .retrieve()
      .bodyToMono(typeReference<Prison>())
      .onErrorResume(WebClientResponseException::class.java) { emptyWhenNotFound(it) }
      .block()

  fun getUserCaseLoads(): List<UserCaseLoad> =
    webClient.get()
      .uri("/api/users/me/caseLoads")
      .retrieve()
      .bodyToMono(typeReference<List<UserCaseLoad>>())
      .block() ?: emptyList()

  fun getPrisonTransfersEnRoute(agencyId: String): List<OffenderMovement> =
    webClient.get()
      .uri("/api/movements/$agencyId/enroute")
      .retrieve()
      .bodyToMono(typeReference<List<OffenderMovement>>())
      .block() ?: emptyList()

  /**
   * The prison-api end-point expects requests to have role 'BOOKING_CREATE' and scope 'write'.
   */
  fun createOffender(detail: CreateOffenderDetail): InmateDetail =
    webClient.post()
      .uri("/api/offenders")
      .bodyValue(detail)
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) { response ->
        propagateClientError(response, "Client error when posting to /api/offenders")
      }
      .bodyToMono(InmateDetail::class.java)
      .block() ?: throw RuntimeException()

  /**
   * The prison-api end-point expects requests to have role 'BOOKING_CREATE', scope 'write' and a (NOMIS) username.
   */
  fun admitOffenderOnNewBooking(offenderNo: String, detail: AdmitOnNewBookingDetail): InmateDetail =
    webClient.post()
      .uri("/api/offenders/$offenderNo/booking")
      .bodyValue(detail)
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) { response ->
        propagateClientError(response, "Client error when posting to /api/offenders/$offenderNo/booking")
      }
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
      .onStatus(HttpStatus::is4xxClientError) { response ->
        propagateClientError(response, "Client error when posting to /api/offenders/$offenderNo/recall")
      }
      .bodyToMono(InmateDetail::class.java)
      .block() ?: throw IllegalStateException("No response from prison api")

  /**
   * The prison-api end-point expects requests to have role 'TRANSFER_PRISONER', scope 'write' and a (NOMIS) username.
   */
  fun transferIn(offenderNo: String, detail: TransferIn): InmateDetail =
    webClient.put()
      .uri("/api/offenders/$offenderNo/transfer-in")
      .bodyValue(detail)
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) { response ->
        propagateClientError(response, "Client error when posting to /api/offenders/$offenderNo/transfer-in")
      }
      .bodyToMono(InmateDetail::class.java)
      .block() ?: throw IllegalStateException("No response from prison api")

  /**
   * The prison-api end-point expects requests to have role 'TRANSFER_PRISONER', scope 'write' and a (NOMIS) username.
   */
  fun confirmTemporaryAbsencesArrival(offenderNo: String, detail: TemporaryAbsencesArrival): InmateDetail =
    webClient.put()
      .uri("/api/offenders/$offenderNo/temporary-absence-arrival")
      .bodyValue(detail)
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) { response ->
        propagateClientError(
          response,
          "Client error when posting to /api/offenders/$offenderNo/temporary-absence-arrival"
        )
      }
      .bodyToMono(InmateDetail::class.java)
      .block() ?: throw IllegalStateException("No response from prison api")

  fun courtTransferIn(prisonNumber: String, detail: CourtTransferIn): InmateDetail =
    webClient.put()
      .uri("/api/offenders/$prisonNumber/court-transfer-in")
      .bodyValue(detail)
      .retrieve()
      .onStatus(HttpStatus::is4xxClientError) { response ->
        propagateClientError(response, "Client error when posting to /api/offenders/$prisonNumber/court-transfer-in")
      }
      .bodyToMono(InmateDetail::class.java)
      .block() ?: throw IllegalStateException("No response from prison api")

  fun getTemporaryAbsences(agencyId: String): List<TemporaryAbsence> =
    webClient.get()
      .uri("/api/movements/agency/$agencyId/temporary-absences")
      .retrieve()
      .bodyToMono(typeReference<List<TemporaryAbsence>>())
      .block() ?: emptyList()

  fun getMovement(agencyId: String, fromDate: LocalDateTime, toDate: LocalDateTime): List<Movement> =
    webClient.get()
      .uri("api/movements/$agencyId/in?fromDateTime=$fromDate&toDateTime=$toDate")
      .header("Page-Limit", "10000")
      .retrieve()
      .bodyToMono(typeReference<List<Movement>>())
      .block() ?: emptyList()
}
