package uk.gov.justice.digital.hmpps.bodyscan.apiclient.model

import java.time.LocalDate

data class OffenderDetails(
  val bookingId: Long,
  val offenderNo: String,
  val firstName: String,
  val lastName: String,
  val agencyId: String,
  val activeFlag: Boolean,
  val dateOfBirth: LocalDate

)
