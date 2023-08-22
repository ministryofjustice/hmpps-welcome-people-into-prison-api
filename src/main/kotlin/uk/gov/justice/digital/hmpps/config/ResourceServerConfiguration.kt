package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher
import org.springframework.web.servlet.handler.HandlerMappingIntrospector

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
class ResourceServerConfiguration {
  @Bean
  fun securityFilterChain(http: HttpSecurity, mvc: MvcRequestMatcher.Builder): SecurityFilterChain =
    http
      .sessionManagement { sessionManagement ->
        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      }
      .csrf { csrfManagement ->
        csrfManagement.disable()
      }
      .authorizeHttpRequests { auth ->
        auth.requestMatchers(
          mvc.pattern("/webjars/**"),
          mvc.pattern("/favicon.ico"),
          mvc.pattern("/csrf"),
          mvc.pattern("/health/**"),
          mvc.pattern("/info"),
          mvc.pattern("/ping"),
          mvc.pattern("/v3/api-docs/**"),
          mvc.pattern("/swagger-ui/**"),
          mvc.pattern("/swagger-ui.html"),
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

  @Bean
  fun mvc(introspector: HandlerMappingIntrospector?): MvcRequestMatcher.Builder? {
    return MvcRequestMatcher.Builder(introspector)
  }
}
