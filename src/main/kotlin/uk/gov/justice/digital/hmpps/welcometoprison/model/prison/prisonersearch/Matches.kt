package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

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
  val pncNumber: String?,
  val firstName: String,
  val middleNames: String,
  val lastName: String,
  val dateOfBirth: LocalDate

)
