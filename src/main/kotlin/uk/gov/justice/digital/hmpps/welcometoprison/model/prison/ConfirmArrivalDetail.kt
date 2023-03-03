package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import java.time.LocalDate

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data for creating an offender record, an associated booking and then adding that offender to a prison's roll")
data class ConfirmArrivalDetail(
  @Schema(
    description = "The offender's PNC (Police National Computer) number.",
    example = "03/11999M",
    pattern = "^([0-9]{2}|[0-9]{4})/[0-9]+[a-zA-Z]$",
    maxLength = 20,
  )
  @field:Length(max = 20)
  @field:Pattern(regexp = "^([0-9]{2}|[0-9]{4})/[0-9]+[a-zA-Z]$", message = "PNC is not valid")
  val pncNumber: String? = null,

  @Schema(
    description = "The offender's Prison number.",
    example = "A1234AA",
    pattern = "^[A-Za-z]\\d{4}[A-Za-z]{2}\$",
    maxLength = 7,
  )
  @field:Length(max = 7)
  @field:Pattern(regexp = "^[A-Za-z]\\d{4}[A-Za-z]{2}\$", message = "Prison number is not valid")
  val prisonNumber: String? = null,

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
    pattern = "^[A-Z|a-z ,.'-]+$",
  )
  @field:Length(max = 35)
  @field:NotBlank
  @field:Pattern(regexp = "^[A-Z|a-z ,.'-]+$", message = "First name is not valid")
  val firstName: String? = null,

  @Schema(
    required = true,
    description = "The offender's date of birth. Must be specified in YYYY-MM-DD format. Range allowed is 16-110 years",
    example = "1970-01-01",
  )
  @field:NotNull
  val dateOfBirth: LocalDate? = null,

  @Schema(
    required = true,
    description = "A code representing the offender's gender (from the SEX reference domain).",
    example = "M",
    allowableValues = ["M", "F", "NK", "NS", "REF"],
    maxLength = 12,
  )
  @field:Length(max = 12)
  @field:NotBlank
  val sex: String? = null,

  @Schema(description = "Received Prison ID", example = "MDI", required = true)
  @field:Length(max = 3, message = "Prison ID is 3 character code")
  @field:NotNull
  val prisonId: String? = null,

  @Schema(description = "Where the prisoner has moved from (default OUT)", example = "OUT")
  @field:Length(max = 6, message = "From location")
  val fromLocationId: String? = null,

  @Schema(description = "Reason for in movement (e.g. Unconvicted Remand)", example = "N")
  @field:NotNull
  val movementReasonCode: String? = null,

  @Schema(description = "Comments", example = "Prisoner arrived from court")
  @field:Length(max = 240, message = "comment text size is a maximum of 240 characters")
  val commentText: String? = null,

  @Schema(description = "Require imprisonment status (e.g Adult Imprisonment Without Option CJA03)", example = "SENT03")
  @field:Length(max = 12, message = "Imprisonment status cannot be more than 12 characters")
  @field:NotNull
  val imprisonmentStatus: String? = null,
) {
  val youthOffender: Boolean
    get() = Age.lessThanTwentyOneYears(dateOfBirth!!, LocalDate.now())

  val isNewToPrison: Boolean
    get() = prisonNumber == null
}
