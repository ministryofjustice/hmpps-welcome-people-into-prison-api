package uk.gov.justice.digital.hmpps.welcometoprison.model

import java.time.LocalDate

data class Movement(
  val firstName: String,
  val lastName: String,
  val dateOfBirth: LocalDate,
  val prisonNumber: String,
  val pncNumber: String,
  val date: LocalDate,
  val moveType: MoveType
)

enum class MoveType {
  PRISON_REMAND,
  COURT_APPEARANCE,
  PRISON_RECALL,
  VIDEO_REMAND,
  PRISON_TRANSFER,
}
