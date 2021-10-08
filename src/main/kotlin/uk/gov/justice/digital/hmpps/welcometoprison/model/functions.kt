package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.springframework.core.ParameterizedTypeReference

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}
