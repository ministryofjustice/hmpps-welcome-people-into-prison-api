package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Matches(
  val matches: List<PrisonerContainer>? = emptyList()
)

data class PrisonerContainer(
  val prisoner: Prisoner
)

data class Prisoner(
  val prisonerNumber: String,
  val pncNumber: String? = null,
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val croNumber: String?,
  val gender: String,
  val status: String?
) {
  val isCurrentPrisoner: Boolean
    get() = status != null && status != INACTIVE_OUT
}
