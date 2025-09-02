package uk.gov.justice.digital.hmpps.bodyscan.apiclient.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanReason
import uk.gov.justice.digital.hmpps.bodyscan.model.BodyScanResult
import java.time.LocalDate

data class PersonalCareNeeds(
  @JsonIgnore
  val date: LocalDate,
  @JsonIgnore
  val bodyScanReason: BodyScanReason,
  @JsonIgnore
  val bodyScanResult: BodyScanResult,

) {
  @JsonProperty("problemCode")
  fun getProblemCode() = "BSC6.0"

  @JsonProperty("problemStatus")
  fun getProblemStatus() = "ON"

  @JsonProperty("startDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  fun getStartDate() = date

  @JsonProperty("commentText")
  fun getCommentText() = ("${bodyScanReason.desc} - $bodyScanResult").lowercase().replaceFirstChar { it.uppercase() }
}

fun PersonalCareNeeds.toEventProperties(prisonNumber: String, username: String) = mapOf(
  "prisonNumber" to prisonNumber,
  "date" to date.toString(),
  "bodyScanReason" to bodyScanReason.toString(),
  "bodyScanResult" to bodyScanResult.toString(),
  "username" to username,

)
