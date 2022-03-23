package uk.gov.justice.digital.hmpps.welcometoprison.validtor

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.validation.Errors
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.request.MatchPrisonersRequest
import uk.gov.justice.digital.hmpps.welcometoprison.validator.MatchPrisonersRequestValidator
import java.time.LocalDate

class MatchPrisonersRequestValidatorTest {

  private val matchPrisonersRequestValidator = MatchPrisonersRequestValidator()

  @Test
  fun `when empty object is provided lastName, pncNumber, prisonNumber fields need to be validated`() {
    val errors: Errors = mock()
    val request = MatchPrisonersRequest()
    matchPrisonersRequestValidator.validate(request, errors)
    verify(errors).rejectValue("lastName", "Last name is required")
    verify(errors).rejectValue("pncNumber", "PNC number is required")
    verify(errors).rejectValue("prisonNumber", "Last name is required")
  }

  @Test
  fun `when DoB is provided last name field need to be validated`() {
    val errors: Errors = mock()
    val request = MatchPrisonersRequest(dateOfBirth = LocalDate.now())
    matchPrisonersRequestValidator.validate(request, errors)
    verify(errors).rejectValue("lastName", "Last name need to be provided together with DoB")
  }

  @Test
  fun `when first name is provided last name field need to be validated`() {
    val errors: Errors = mock()
    val request = MatchPrisonersRequest(firstName = "Tom", prisonNumber = "AAA123")
    matchPrisonersRequestValidator.validate(request, errors)
    verify(errors).rejectValue("lastName", "Last name need to be provided together with first name")
  }
}
