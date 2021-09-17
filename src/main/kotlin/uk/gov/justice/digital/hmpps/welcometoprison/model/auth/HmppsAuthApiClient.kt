package uk.gov.justice.digital.hmpps.welcometoprison.model.auth

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class HmppsAuthApiClient(@Qualifier("oauthApiWebClient") private val webClient: WebClient)
