package uk.gov.justice.digital.hmpps.welcometoprison.validator

import org.springframework.stereotype.Service
import org.springframework.validation.Errors
import org.springframework.validation.Validator
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest

@Service
class MatchPrisonersRequestValidator : Validator {
  override fun supports(clazz: Class<*>): Boolean {
    return MatchPrisonersRequest::class.java.equals(clazz)
  }

  override fun validate(target: Any, errors: Errors) {
    val matchPrisonersRequest = target as MatchPrisonersRequest

    if (matchPrisonersRequest.dateOfBirth == null &&
      matchPrisonersRequest.prisonNumber == null &&
      matchPrisonersRequest.pncNumber == null &&
      matchPrisonersRequest.lastName == null
    ) {
      errors.rejectValue("lastName", "Last name is required")
      errors.rejectValue("pncNumber", "PNC number is required")
      errors.rejectValue("prisonNumber", "Last name is required")
      return
    }

    if (matchPrisonersRequest.dateOfBirth != null && matchPrisonersRequest.lastName == null) {
      errors.rejectValue("lastName", "Last name need to be provided together with DoB")
      return
    }
    if (matchPrisonersRequest.firstName != null && matchPrisonersRequest.lastName == null) {
      errors.rejectValue("lastName", "Last name need to be provided together with first name")
      return
    }
  }
}
