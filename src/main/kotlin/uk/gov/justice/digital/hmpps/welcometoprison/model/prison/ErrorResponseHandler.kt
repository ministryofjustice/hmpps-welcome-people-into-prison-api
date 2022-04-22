package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.nio.charset.Charset

class ErrorResponseHandler : WebClientResponseException {

 // private val mapper = jacksonObjectMapper()
  //  .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  constructor(
    statusCode: Int,
    statusText: String?,
    headers: HttpHeaders?,
    body: ByteArray?,
    charset: Charset?
  ) : super(statusCode, statusText, headers, body, charset)

  constructor(
    status: Int,
    reasonPhrase: String?,
    headers: HttpHeaders?,
    body: ByteArray?,
    charset: Charset?,
    request: HttpRequest?
  ) : super(status, reasonPhrase, headers, body, charset, request)

  constructor(
    message: String?,
    statusCode: Int,
    statusText: String?,
    headers: HttpHeaders?,
    responseBody: ByteArray?,
    charset: Charset?
  ) : super(message, statusCode, statusText, headers, responseBody, charset)

  constructor(
    message: String?,
    statusCode: Int,
    statusText: String?,
    headers: HttpHeaders?,
    responseBody: ByteArray?,
    charset: Charset?,
    request: HttpRequest?
  ) : super(message, statusCode, statusText, headers, responseBody, charset, request)

  fun getErrorResponse(): ErrorResponse? {
    return ErrorResponse()
   // return mapper.readValue(this.responseBodyAsString, ErrorResponse::class.java)
  }
}

class ErrorResponse {
  val status: Int? = null
  val errorCode: Int? = null
  val userMessage: String? = null
  val developerMessage: String? = null
  val moreInfo: String? = null
}