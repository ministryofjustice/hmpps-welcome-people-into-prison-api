package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data for creating an offender record, an associated booking and then adding that offender to a prison's roll")
data class ConfirmArrivalDetail(
  @Schema(
    description = "The offender's PNC (Police National Computer) number.",
    example = "03/11999M",
    pattern = "^([0-9]{2}|[0-9]{4})/[0-9]+[a-zA-Z]$",
    maxLength = 20
  )
  @field:Length(max = 20)
  @field:Pattern(regexp = "^([0-9]{2}|[0-9]{4})/[0-9]+[a-zA-Z]$", message = "PNC is not valid")
  val pncNumber: String? = null,

  @Schema(required = true, description = "The offender's last name.", example = "Mark", maxLength = 35)
  @field:Length(max = 35)
  @field:NotBlank
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Last name is not valid")
  val lastName: String? = null,

  @Schema(
    required = true,
    description = "The offender's first name.",
    example = "John",
    maxLength = 35,
    pattern = "^[A-Z|a-z ,.'-]+$"
  )
  @field:Length(max = 35)
  @field:NotBlank
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "First name is not valid")
  val firstName: String? = null,

  @Schema(description = "The offender's middle name.", example = "Luke", maxLength = 35, pattern = "^[A-Z|a-z ,.'-]+$")
  @field:Length(max = 35)
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Middle name is not valid")
  val middleName1: String? = null,

  @Schema(
    description = "An additional middle name for the offender.",
    example = "Matthew",
    maxLength = 35,
    pattern = "^[A-Z|a-z ,.'-]+$"
  )
  @field:Length(max = 35)
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "Middle name 2 is not valid")
  val middleName2: String? = null,

  @Schema(
    description = "A code representing the offender's title (from TITLE reference domain).",
    example = "MR",
    allowableValues = ["BR", "DAME", "DR", "FR", "IMAM", "LADY", "LORD", "MISS", "MR", "MRS", "MS", "RABBI", "REV", "SIR", "SR"],
    maxLength = 12
  )
  @field:Length(max = 12)
  val title: String? = null,

  @Schema(
    description = "A code representing a suffix to apply to offender's name (from SUFFIX reference domain).",
    example = "JR",
    maxLength = 12,
    allowableValues = ["I", "II", "III", "IV", "IX", "V", "VI", "VII", "VIII", "JR", "SR"]
  )
  @field:Length(max = 12)
  val suffix: String? = null,

  @Schema(
    required = true,
    description = "The offender's date of birth. Must be specified in YYYY-MM-DD format. Range allowed is 16-110 years",
    example = "1970-01-01"
  )
  @field:NotNull
  val dateOfBirth: LocalDate? = null,

  @Schema(
    required = true,
    description = "A code representing the offender's gender (from the SEX reference domain).",
    example = "M",
    allowableValues = ["M", "F", "NK", "NS", "REF"],
    maxLength = 12
  )
  @field:Length(max = 12)
  @field:NotBlank
  val gender: String? = null,

  @Schema(
    description = "A code representing the offender's ethnicity (from the ETHNICITY reference domain).",
    example = "W1",
    allowableValues = ["A9", "B1", "B2", "B9", "M1", "M2", "M3", "M9", "NS", "O1", "O2", "O9", "W1", "W2", "W3", "W8", "W9"],
    maxLength = 12
  )
  @field:Length(max = 12)
  val ethnicity: String? = null,

  @Schema(description = "The offender's CRO (Criminal Records Office) number.", maxLength = 20)
  @field:Length(max = 20)
  val croNumber: String? = null,

  @Schema(name = "Received Prison ID", example = "MDI", required = true)
  @field:Length(max = 3, message = "Prison ID is 3 character code")
  @field:NotNull
  val prisonId: String? = null,

  @Schema(
    required = true,
    name = "The time the booking in occurred, if not supplied it will be the current time",
    example = "2020-03-24T12:13:40"
  )
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  val bookingInTime: LocalDateTime? = null,

  @Schema(name = "Where the prisoner has moved from (default OUT)", example = "OUT")
  @field:Length(max = 6, message = "From location")
  val fromLocationId: String? = null,

  @Schema(name = "Reason for in movement (e.g. Unconvicted Remand)", example = "N")
  @field:NotNull
  val movementReasonCode: String? = null,

  @Schema(
    name = "Cell location where recalled prisoner should be housed, default will be reception",
    example = "MDI-RECP",
  )
  @field:Length(max = 240, message = "Cell Location description cannot be more than 240 characters")
  val cellLocation: String? = null,

  @Schema(name = "Require imprisonment status (e.g Adult Imprisonment Without Option CJA03)", example = "SENT03")
  @field:Length(max = 12, message = "Imprisonment status cannot be more than 12 characters")
  val imprisonmentStatus: String? = null
) {
  val youthOffender: Boolean
    get() = Age.lessThanTwentyOneYears(dateOfBirth!!, LocalDate.now())
}
