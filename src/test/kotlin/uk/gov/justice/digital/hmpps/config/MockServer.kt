package uk.gov.justice.digital.hmpps.config

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration

open class MockServer(port: Int) : WireMockServer(
  WireMockConfiguration.wireMockConfig()
    .port(port),
)
