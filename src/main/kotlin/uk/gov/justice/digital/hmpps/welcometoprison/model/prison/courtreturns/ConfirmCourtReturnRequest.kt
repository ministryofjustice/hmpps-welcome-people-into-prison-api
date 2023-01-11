package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data for creating an offender record, an associated booking and then adding that offender to a prison's roll")
data class ConfirmCourtReturnRequest(

  @Schema(description = "Received Prison ID", example = "MDI", required = true)
  @field:Length(max = 3, message = "Prison ID is 3 character code")
  @field:NotNull
  val prisonId: String,

  @Schema(
    description = "The offender's Prison number.",
    example = "A1234AA",
    pattern = "^[A-Za-z]\\d{4}[A-Za-z]{2}\$",
    maxLength = 7
  )
  @field:Length(max = 7)
  @field:Pattern(regexp = "^[A-Za-z]\\d{4}[A-Za-z]{2}\$", message = "Prison number is not valid")
  @field:NotNull
  val prisonNumber: String
)
