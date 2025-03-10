package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime

@JsonInclude(NON_NULL)
@Schema(description = "Recent arrivals")
data class RecentArrival(
  @Schema(description = "Prison number", example = "G4156GV") val prisonNumber: String,
  @Schema(description = "Date of Birth", example = "1996-07-23") val dateOfBirth: LocalDate,
  @Schema(description = "Fist name", example = "John") val firstName: String,
  @Schema(description = "Last name", example = "Brown") val lastName: String,
  @Schema(description = "Arrival date and time ", example = "2022-01-18T08:00:00") val movementDateTime: LocalDateTime,
  @Schema(description = "Location", example = "MDI-1-3-004") val location: String?,
) : Comparable<RecentArrival> {

  override fun compareTo(other: RecentArrival) = compareByDescending<RecentArrival> { it.movementDateTime }
    .thenBy { it.lastName }
    .thenBy { it.firstName }
    .thenBy { it.prisonNumber }
    .compare(this, other)
}
