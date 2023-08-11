package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration {
  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http
      .sessionManagement { sessionManagement ->
        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      }
      .csrf { csrfManagement ->
        csrfManagement.disable()
      }
      .authorizeHttpRequests { auth ->
        auth.requestMatchers(
          "/webjars/**", "/favicon.ico", "/csrf",
          "/health/**", "/info", "/ping",
          "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
        )
          .permitAll().anyRequest().authenticated()
      }
      .also {
        it.oauth2ResourceServer { oauth2ResourceServerCustomizer ->
          oauth2ResourceServerCustomizer.jwt { jwtCustomizer ->
            jwtCustomizer.jwtAuthenticationConverter(AuthAwareTokenConverter())
          }
        }
      }
      .build()
}
