package uk.gov.justice.digital.hmpps.config

import io.jsonwebtoken.Jwts
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.stereotype.Component
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.time.Duration
import java.util.*

@Component
class JwtAuthHelper {
  private final var keyPair: KeyPair

  init {
    val gen = KeyPairGenerator.getInstance("RSA")
    gen.initialize(2048)
    keyPair = gen.generateKeyPair()
  }

  @Bean
  fun jwtDecoder(): JwtDecoder = NimbusJwtDecoder.withPublicKey(keyPair.public as RSAPublicKey).build()

  fun setAuthorisation(
    user: String?,
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
    clientId: String = "test-client-id",
  ): (HttpHeaders) -> Unit {
    val token = createJwt(
      subject = user,
      scope = scopes,
      expiryTime = Duration.ofHours(1L),
      roles = roles,
      clientId = clientId,
    )
    return { it.set(HttpHeaders.AUTHORIZATION, "Bearer $token") }
  }

  fun getAuthorisation(
    user: String,
    roles: List<String> = listOf(),
    scopes: List<String> = listOf(),
    clientId: String = "test-client-id",
  ): String {
    val token = createJwt(
      subject = user,
      scope = scopes,
      expiryTime = Duration.ofHours(1L),
      roles = roles,
      clientId = clientId,
    )
    return "Bearer $token"
  }

  internal fun createJwt(
    clientId: String,
    subject: String?,
    scope: List<String>? = listOf(),
    roles: List<String>? = listOf(),
    expiryTime: Duration = Duration.ofHours(1),
    jwtId: String = UUID.randomUUID().toString(),
    authSource: String = "none",
    grantType: String = "client_credentials",
  ): String = mutableMapOf<String, Any>(
    "sub" to (subject ?: clientId),
    "client_id" to clientId,
    "auth_source" to authSource,
    "grant_type" to grantType,
  ).apply {
    subject?.let { this["user_name"] = subject }
    scope?.let { this["scope"] = scope }
    roles?.let {
      this["authorities"] = roles.map { "ROLE_${it.substringAfter("ROLE_")}" }
    }
  }
    .let {
      Jwts.builder()
        .id(jwtId)
        .subject(subject ?: clientId)
        .claims(it.toMap())
        .expiration(Date(System.currentTimeMillis() + expiryTime.toMillis()))
        .signWith(keyPair.private)
        .compact()
    }
}
