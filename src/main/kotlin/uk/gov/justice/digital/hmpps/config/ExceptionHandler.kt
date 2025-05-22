package uk.gov.justice.digital.hmpps.config

import jakarta.validation.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestValueException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.digitalprisonreportinglib.exception.UserAuthorisationException

@RestControllerAdvice
class ExceptionHandler {
  @ExceptionHandler(ValidationException::class)
  fun handleValidationException(e: ValidationException): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
    log.info("Validation exception: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = "Validation failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(NotFoundException::class, NoResourceFoundException::class)
  fun handleNotFoundException(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Not found exception: {}", e.message)
    return ResponseEntity
      .status(NOT_FOUND)
      .body(
        ErrorResponse(
          status = NOT_FOUND.value(),
          userMessage = "Resource not found",
          developerMessage = e.message,
        ),
      )
  }

  @ExceptionHandler(ConflictException::class)
  fun handleConflictException(e: Exception): ResponseEntity<ErrorResponse> {
    log.warn("Conflict exception: {}", e.message)
    return ResponseEntity
      .status(CONFLICT)
      .body(
        ErrorResponse(
          status = CONFLICT.value(),
          userMessage = "Resource in illegal state",
          developerMessage = "Resource in illegal state",
        ),
      )
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDenied(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Access denied exception: {}", e.message)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN.value(),
          userMessage = "Access denied",
        ),
      )
  }

  @ExceptionHandler(MissingRequestValueException::class)
  fun handleMissingRequestValue(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Missing request value: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = "Missing request value",
        ),
      )
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException::class)
  fun handleMethodArgMismatch(e: Exception): ResponseEntity<ErrorResponse> {
    log.info("Argument mismatch: {}", e.message)
    return ResponseEntity
      .status(BAD_REQUEST)
      .body(
        ErrorResponse(
          status = BAD_REQUEST.value(),
          userMessage = "Argument type mismatch",
        ),
      )
  }

  @ExceptionHandler(ClientException::class)
  fun handleClientException(e: ClientException): ResponseEntity<ErrorResponse> {
    log.warn(
      "Client exception: message {} status {} errorCode {} userMessage {} developerMessage {} moreInfo {}",
      e.message,
      e.response.status,
      e.response.errorCode,
      e.response.userMessage,
      e.response.developerMessage,
      e.response.moreInfo,
      e,
    )
    val message = "Exception calling up-stream service from Wpip-Api"
    return ResponseEntity
      .status(e.response.status ?: INTERNAL_SERVER_ERROR.value())
      .body(
        ErrorResponse(
          status = e.response.status,
          errorCode = e.errorCode,
          userMessage = message,
          developerMessage = message,
        ),
      )
  }

  @ExceptionHandler(java.lang.Exception::class)
  fun handleException(e: java.lang.Exception): ResponseEntity<ErrorResponse?>? {
    log.error("Unexpected exception", e)
    return ResponseEntity
      .status(INTERNAL_SERVER_ERROR)
      .body(
        ErrorResponse(
          status = INTERNAL_SERVER_ERROR.value(),
          userMessage = "Unexpected error",
        ),
      )
  }

  @ExceptionHandler(UserAuthorisationException::class)
  @ResponseStatus(FORBIDDEN)
  fun handleUserAuthorisationException(e: UserAuthorisationException): ResponseEntity<ErrorResponse> {
    log.error("Access denied exception: {}", e.message)
    return ResponseEntity
      .status(FORBIDDEN)
      .body(
        ErrorResponse(
          status = FORBIDDEN.value(),
          userMessage = "User authorisation failure: ${e.message}",
          developerMessage = e.message,
        ),
      )
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}

data class ErrorResponse(
  val status: Int? = null,
  val errorCode: ErrorCode? = null,
  val userMessage: String? = null,
  val developerMessage: String? = null,
  val moreInfo: String? = null,
)
