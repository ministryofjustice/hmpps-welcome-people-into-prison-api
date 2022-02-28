package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import java.time.LocalDate

@JsonInclude(NON_NULL)
data class Prisoner(
  val firstName: String,
  val lastName: String,

  @JsonFormat(pattern = "yyyy-MM-dd")
  val dateOfBirth: LocalDate,
  val prisonerNumber: String,
  val pncNumber: String?,
  val croNumber: String?
)
