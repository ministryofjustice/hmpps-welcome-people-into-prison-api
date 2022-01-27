package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate

@JsonInclude(NON_NULL)
@Schema(description = "A movement into prison")
data class Arrival(
  @Schema(description = "Movement ID", example = "0573de83-8a29-42aa-9ede-1068bc433fc5")
  val id: String?,

  @Schema(description = "First name", example = "Sam")
  val firstName: String,

  @Schema(description = "Last name", example = "Smith")
  val lastName: String,

  @Schema(description = "Date of birth", example = "1971-02-01")
  val dateOfBirth: LocalDate,

  @Schema(description = "Prison number", example = "A1234AA")
  val prisonNumber: String?,

  @Schema(description = "PNC number", example = "01/1234X")
  val pncNumber: String?,

  @Schema(description = "Date expected", example = "2020-02-23")
  val date: LocalDate,

  @Schema(description = "From Location", example = "Kingston-upon-Hull Crown Court")
  val fromLocation: String,

  @Schema(description = "From location type", example = "COURT")
  val fromLocationType: LocationType,

  @Schema(description = "Is the arrival in custody according to NOMIS", example = "true")
  val isCurrentPrisoner: Boolean = false,

  @Schema(
    description = "Gender of the arrival according to Book a Secure Move. Only returns MALE or FEMALE. If Book a Secure Move returns Trans, this resource will return null as we will need to capture legal gender information",
    example = "MALE"
  )
  val gender: Gender? = null,

  @Schema(
    description = "A list of all potential matches",
    example = "[firstName: \"Sam\",lastName: \"Smith\",dateOfBirth: \"1971-02-01\",prisonNumber: \"A1234BC\",pncNumber: \"01/1234X\"]"
  )
  var potentialMatches: List<PotentialMatch>? = null
)

data class PotentialMatch(
  @Schema(description = "First name", example = "Sam")
  val firstName: String,

  @Schema(description = "Last name", example = "Smith")
  val lastName: String,

  @Schema(description = "Date of birth", example = "1971-02-01")
  val dateOfBirth: LocalDate,

  @Schema(description = "Prison number", example = "A1234AA")
  val prisonNumber: String?,

  @Schema(description = "PNC number", example = "01/1234X")
  val pncNumber: String?
)

enum class LocationType {
  COURT,
  CUSTODY_SUITE,
  PRISON,
  OTHER
}

enum class Gender {
  MALE,
  FEMALE,
  TRANS,
}
