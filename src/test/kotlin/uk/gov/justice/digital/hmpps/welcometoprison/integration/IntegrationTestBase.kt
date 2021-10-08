package uk.gov.justice.digital.hmpps.welcometoprison.integration

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Suppress("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthHelper

  companion object {
    internal val basmApiMockServer = BasmApiMockServer()
    internal val prisonApiMockServer = PrisonApiMockServer()
    internal val hmppsAuthMockServer = HmppsAuthMockServer()
    internal val prisonerSearchMockServer = PrisonerSearchMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      basmApiMockServer.start()
      basmApiMockServer.stubGrantToken()
      basmApiMockServer.stubGetPrison(200)
      basmApiMockServer.stubGetMovements(200)
      prisonApiMockServer.start()
      basmApiMockServer.stubGetMovement(200)
      prisonApiMockServer.start()
      prisonerSearchMockServer.start()
      prisonerSearchMockServer.stubMatchPrisoners(200)
      hmppsAuthMockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      basmApiMockServer.stop()
      prisonApiMockServer.stop()
      prisonerSearchMockServer.stop()
      hmppsAuthMockServer.stop()
    }
  }

  init {
    // Resolves an issue where Wiremock keeps previous sockets open from other tests causing connection resets
    System.setProperty("http.keepAlive", "false")
  }

  @BeforeEach
  fun resetStubs() {
    hmppsAuthMockServer.resetAll()
    prisonApiMockServer.resetAll()
    prisonerSearchMockServer.resetAll()

    hmppsAuthMockServer.stubGrantToken()
  }

  internal fun setAuthorisation(
    user: String = "court-reg-client",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf()
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisation(user, roles, scopes)
}
