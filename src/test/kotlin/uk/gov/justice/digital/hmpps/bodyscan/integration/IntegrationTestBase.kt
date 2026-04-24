package uk.gov.justice.digital.hmpps.bodyscan.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJson
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@AutoConfigureJson
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  lateinit var webTestClient: WebTestClient

  val objectMapper: ObjectMapper by lazy { ObjectMapper().registerModule(JavaTimeModule()) }

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  companion object {
    internal val prisonApiMockServer = PrisonApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      prisonApiMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      prisonApiMockServer.stop()
    }
  }

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  @BeforeEach
  fun resetStubs() {
    prisonApiMockServer.resetAll()
  }

  internal fun <S : RequestHeadersSpec<S>> RequestHeadersSpec<S>.withBearerToken(token: String) = this.apply { header(AUTHORIZATION, token) }

  protected fun setAuthorisation(
    user: String? = "WELCOME_INTO_PRISON_API",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(
    clientId = "welcome-into-prison-client",
    username = user,
    roles = roles,
    scope = scopes,
  )
}
