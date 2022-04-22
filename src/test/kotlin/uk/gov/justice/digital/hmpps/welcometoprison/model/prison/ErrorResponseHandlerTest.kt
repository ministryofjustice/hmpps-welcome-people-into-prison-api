package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

class ErrorResponseHandlerTest {

 /* @Test
  fun `successfulErrorResponseConversion`() {
    val json = """
             {
    "status": 409,
    "errorCode" : 30001,
    "userMessage": "The cell MDI-RECP does not have any available capacity",
    "developerMessage": "The cell MDI-RECP does not have any available capacity"
}
            """
    val headers = mock<HttpHeaders>()
    val responseBody = json.toByteArray()
    val charset = Charset.forName("UTF-8");
    val errorResponseHandler = ErrorResponseHandler(409, "test", headers, responseBody, charset)
    val errorResponse = errorResponseHandler.getErrorResponse()
    Assertions.assertThat(errorResponse?.errorCode).isEqualTo(30001)
    Assertions.assertThat(errorResponse?.status).isEqualTo(409)
  }
*/
}
