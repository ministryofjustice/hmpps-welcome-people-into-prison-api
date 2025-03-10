package uk.gov.justice.digital.hmpps.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class Configuration {
  @Bean
  fun clock(): Clock = Clock.systemDefaultZone()
}
