package uk.gov.justice.digital.hmpps.welcometoprison.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.RequestHeadersSpec
import uk.gov.justice.digital.hmpps.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals.ConfirmedArrivalRepository

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(JwtAuthHelper::class)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var confirmedArrivalRepository: ConfirmedArrivalRepository

  val objectMapper: ObjectMapper by lazy { ObjectMapper().registerModule(JavaTimeModule()) }

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  companion object {
    internal val basmApiMockServer = BasmApiMockServer()
    internal val prisonApiMockServer = PrisonApiMockServer()
    internal val prisonRegisterMockServer = PrisonRegisterMockServer()
    internal val prisonerSearchMockServer = PrisonerSearchMockServer()
    internal val manageUsersApiMockServer = ManageUsersApiMockServer()
    internal val hmppsAuthMockServer = HmppsAuthMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      basmApiMockServer.start()
      basmApiMockServer.stubGrantToken()
      basmApiMockServer.stubGetPrison(200)
      basmApiMockServer.stubGetMovements(200)
      basmApiMockServer.stubGetMovement("testId", 200)
      prisonApiMockServer.start()
      prisonRegisterMockServer.start()
      prisonerSearchMockServer.start()
      prisonerSearchMockServer.stubMatchPrisoners(200)
      manageUsersApiMockServer.start()
      hmppsAuthMockServer.start()
      hmppsAuthMockServer.stubGrantToken()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      basmApiMockServer.stop()
      prisonApiMockServer.stop()
      prisonRegisterMockServer.stop()
      prisonerSearchMockServer.stop()
      manageUsersApiMockServer.stop()
      hmppsAuthMockServer.stop()
    }
  }

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  @BeforeEach
  fun resetStubs() {
    prisonApiMockServer.resetAll()
    prisonRegisterMockServer.resetAll()
    prisonerSearchMockServer.resetAll()
    manageUsersApiMockServer.resetAll()
  }

  internal fun <S : RequestHeadersSpec<S>?> RequestHeadersSpec<S>.withBearerToken(token: String) = this.apply { header(AUTHORIZATION, token) }

  internal fun setAuthorisation(
    user: String? = "welcome-into-prison-client",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles, scopes)

  internal fun getAuthorisation(
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
    username: String = "welcome-into-prison-client",
  ) = jwtAuthHelper.getAuthorisation(user = username, roles = roles, scopes = scopes)
}
