package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.courtreturns

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data for creating an offender record, an associated booking and then adding that offender to a prison's roll")
data class ConfirmCourtReturnRequest(

  @Schema(description = "Received Prison ID", example = "MDI", required = true)
  @field:Length(max = 3, message = "Prison ID is 3 character code")
  @field:NotNull
  val prisonId: String? = null
)