package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType
import java.time.LocalDate

@JsonInclude(NON_NULL)
@Schema(description = "A movement into prison")
data class Arrival(
  @Schema(description = "ID of the Arrival", example = "0573de83-8a29-42aa-9ede-1068bc433fc5")
  val id: String?,

  @Schema(description = "First name", example = "Sam")
  val firstName: String,

  @Schema(description = "Last name", example = "Smith")
  val lastName: String,

  @Schema(description = "Date of birth", example = "1971-02-01")
  val dateOfBirth: LocalDate?,

  @Schema(description = "Prison number", example = "A1234AA")
  val prisonNumber: String?,

  @Schema(description = "PNC number", example = "01/1234X")
  val pncNumber: String?,

  @Schema(description = "Date expected", example = "2020-02-23")
  val date: LocalDate,

  @Schema(description = "From Location", example = "Kingston-upon-Hull Crown Court")
  val fromLocation: String,

  @Schema(description = "Agency ID of From Location", example = "MDI")
  val fromLocationId: String?,

  @Schema(description = "From location type", example = "COURT")
  val fromLocationType: LocationType,

  @Schema(description = "Is the arrival in custody according to NOMIS", example = "true")
  val isCurrentPrisoner: Boolean = false,

  @Schema(
    description = "Gender of the arrival according to Book a Secure Move",
    example = "MALE"
  )
  val gender: Gender? = null,

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
  val prisonNumber: String,

  @Schema(description = "PNC number", example = "01/1234X")
  val pncNumber: String?,

  @Schema(description = "CRO number", example = "29906/12J")
  val croNumber: String?,

  @Schema(description = "Sex", example = "Female")
  val sex: String,

  @JsonIgnore
  val isCurrentPrisoner: Boolean,

  @Schema(description = "Description of arrival type", example = "ACTIVE IN-ADM-MDI")
  val arrivalTypeDescription: String,

  @Schema(description = "Type of arrival", example = "TRANSFER")
  val arrivalType: ArrivalType,
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
