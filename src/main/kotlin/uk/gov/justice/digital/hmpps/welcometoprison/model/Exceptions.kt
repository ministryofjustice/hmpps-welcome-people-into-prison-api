package uk.gov.justice.digital.hmpps.welcometoprison.model

import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.ErrorResponse

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
  val response: ErrorResponse,
  override val message: String,
) : RuntimeException(message) {
  val errorCode: ErrorCode? = ErrorCode.valueOf(response.errorCode)
}
