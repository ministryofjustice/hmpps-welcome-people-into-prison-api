package uk.gov.justice.digital.hmpps.bodyscan.apiclient.model

import java.time.LocalDate

data class SentenceDetails(
  val bookingId: Long,
  val offenderNo: String,
  val firstName: String,
  val lastName: String,
  val agencyLocationId: String,
  val mostRecentActiveBooking: Boolean,
  val dateOfBirth: LocalDate,
  val agencyLocationDesc: String?,
  val internalLocationDesc: String?
)
