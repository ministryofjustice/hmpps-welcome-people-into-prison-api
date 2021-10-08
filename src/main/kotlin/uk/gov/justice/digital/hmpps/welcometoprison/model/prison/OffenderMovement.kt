package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import java.time.LocalDate
import java.time.LocalTime

data class OffenderMovement(
  val offenderNo: String,
  val bookingId: Long,
  val dateOfBirth: LocalDate,
  val firstName: String,
  val middleName: String? = null,
  val lastName: String,
  val fromAgency: String,
  val fromAgencyDescription: String,
  val toAgency: String,
  val toAgencyDescription: String,
  val movementType: String,
  val movementTypeDescription: String,
  val movementReason: String,
  val movementReasonDescription: String,
  val directionCode: String,
  val movementTime: LocalTime,
  val movementDate: LocalDate
)
