package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

data class NewBookingDetail(
  @Schema(name = "Received Prison ID", example = "MDI")
  @Length(max = 3, message = "Prison ID is 3 character code")
  @NotNull
  val prisonId: String? = null,

  @Schema(
    required = true,
    name = "The time the booking in occurred, if not supplied it will be the current time",
    example = "2020-03-24T12:13:40"
  )
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  val bookingInTime: LocalDateTime? = null,

  @Schema(name = "Where the prisoner has moved from (default OUT)", example = "OUT")
  @Length(max = 6, message = "From location")
  val fromLocationId: String? = null,

  @Schema(name = "Reason for in movement (e.g. Unconvicted Remand)", example = "N")
  @NotNull
  val movementReasonCode: String? = null,

  @Schema(name = "Is this offender a youth", example = "false")
  val youthOffender: Boolean = false,

  @Schema(
    name = "Cell location where recalled prisoner should be housed, default will be reception",
    example = "MDI-RECP",
  )
  @Length(max = 240, message = "Cell Location description cannot be more than 240 characters")
  val cellLocation: String? = null,

  @Schema(name = "Require imprisonment status (e.g Adult Imprisonment Without Option CJA03)", example = "SENT03")
  @Length(max = 12, message = "Imprisonment status cannot be more than 12 characters")
  val imprisonmentStatus: String? = null
)
