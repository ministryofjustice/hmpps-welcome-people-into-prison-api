package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException

enum class ErrorCode {
  PRISONER_ALREADY_EXIST, NO_CELL_CAPACITY;

  companion object {
    @JvmStatic
    fun valueOf(errorCode: Int?): ErrorCode? {
      return when (errorCode) {
        30001 -> NO_CELL_CAPACITY
        30002 -> PRISONER_ALREADY_EXIST
        else -> null
      }
    }
  }
}

data class NotFoundException(override val message: String) : RuntimeException(message)

data class ClientException(
  override val cause: WebClientResponseException,
  override val message: String,
  val errorCode: ErrorCode?
) : RuntimeException(message, cause) {
  val httpStatusCode: HttpStatus by cause::statusCode
}
