package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.WebClientResponseException

data class NotFoundException(override val message: String) : RuntimeException(message)

data class ClientException(override val cause: WebClientResponseException, override val message: String) : RuntimeException(message, cause) {
  val httpStatusCode: HttpStatus by cause::statusCode
}
