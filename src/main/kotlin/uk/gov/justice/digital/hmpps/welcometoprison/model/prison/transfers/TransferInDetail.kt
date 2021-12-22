package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.Length
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Data for creating transferring in an offender and adding to the prison roll")
data class TransferInDetail(
  @Schema(
    name = "Cell location where transferred prisoner should be housed, default will be reception",
    example = "MDI-RECP",
  )
  @field:Length(max = 240, message = "Cell Location description cannot be more than 240 characters")
  val cellLocation: String? = null,

  @Schema(
    name = "Additional comments about the transfer",
    example = "Prisoner was transferred in from HMP Nottingham",
  )
  val commentText: String? = null,

  @Schema(
    name = "The time the booking in occurred, if not supplied it will be the current time",
    example = "2020-03-24T12:13:40"
  )
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  val receiveTime: LocalDateTime? = null
)
