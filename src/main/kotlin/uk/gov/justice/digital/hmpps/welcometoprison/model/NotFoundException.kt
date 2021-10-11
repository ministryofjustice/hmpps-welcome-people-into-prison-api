package uk.gov.justice.digital.hmpps.welcometoprison.model

data class NotFoundException(override val message: String) : RuntimeException(message)
