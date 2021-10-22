package uk.gov.justice.digital.hmpps.welcometoprison.model.booking

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@JsonInclude(NON_NULL)
@Schema(description = "Booking made by the service")
@Entity
data class Booking(

  @Schema(description = "ID", example = "")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,
  @Schema(description = "Prison Id", example = "STL")
  val prisonId: String,
  @Schema(description = "Movement Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val movementId: String,
  @Schema(description = "Timestamp")
  val timestamp: LocalDateTime,
  @Schema(description = "Movement Type")
  val moveType: String,
  @Schema(description = "Prisoner Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val prisonerId: String,
  @Schema(description = "Booking Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val bookingId: String?,
  @Schema(description = "Booking Date", example = "2020-12-01")
  val bookingDate: LocalDate
)
