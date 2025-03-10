package uk.gov.justice.digital.hmpps.config

enum class ErrorCode {
  PRISONER_ALREADY_EXIST,
  NO_CELL_CAPACITY,
  ;

  companion object {
    @JvmStatic
    fun valueOf(errorCode: Int?): ErrorCode? = when (errorCode) {
      30001 -> NO_CELL_CAPACITY
      30002 -> PRISONER_ALREADY_EXIST
      else -> null
    }
  }
}

data class ClientErrorResponse(
  val status: Int? = null,
  val errorCode: Int? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
)

data class NotFoundException(override val message: String) : RuntimeException(message)

data class ClientException(
  val response: ClientErrorResponse,
  override val message: String,
) : RuntimeException(message) {
  val errorCode: ErrorCode? = ErrorCode.valueOf(response.errorCode)
}

data class ConflictException(override val message: String) : RuntimeException(message)
