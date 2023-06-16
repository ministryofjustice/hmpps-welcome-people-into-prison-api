package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

data class OffenderMovementWithPnc(
  val offenderMovement: OffenderMovement,
  val pncNumber: String?,
)
