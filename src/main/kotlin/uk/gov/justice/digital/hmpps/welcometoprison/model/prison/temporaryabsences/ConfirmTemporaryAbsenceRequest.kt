package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.temporaryabsences

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Length
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data for confirming offender return from temporary absence to the prison roll")
data class ConfirmTemporaryAbsenceRequest(
  @Schema(
    description = "Prison Id where offender return",
    example = "MDI",
  )
  @field:Length(max = 20, min = 2, message = "Prison identifier cannot be less then 2 and more than 20 characters")
  @field:NotNull
  val prisonId: String,

  @Schema(
    description = "Movement Reason Code",
    example = "CA",
  )
  @field:Length(
    max = 20,
    min = 1,
    message = "Movement reason code cannot be less then 2 and more than 20 characters, if not supplied it will use the previous movement reason"
  )
  val movementReasonCode: String? = null,

  @Schema(
    description = "Additional comments",
    example = "Prisoner was transferred from HMP Nottingham",
  )
  @field:Length(max = 240, message = "comment text size is a maximum of 240 characters")
  val commentText: String? = null,

  @Schema(
    description = "The time the booking in occurred, if not supplied it will use the current time",
    example = "2020-03-24T12:13:40"
  )
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  val receiveTime: LocalDateTime? = null,

  @Schema(description = "The Id of the arrival", example = "123e4567-e89b-12d3-a456-426614174000")
  val arrivalId: String? = null,
)
