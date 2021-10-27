package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Schema(description = "Booking made by the service")
@Entity
data class ConfirmedArrival(

  @Schema(description = "ID", example = "")
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,
  @Schema(description = "Prison Id", example = "STL")
  val prisonNumber: String,
  @Schema(description = "Movement Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val movementId: String,
  @Schema(description = "Timestamp")
  val timestamp: LocalDateTime,
  @Schema(description = "Arrival Type")
  @Enumerated(EnumType.STRING)
  val arrivalType: ArrivalType,
  @Schema(description = "Prisoner Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val prisonerId: String,
  @Schema(description = "Booking Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val bookingId: String?,
  @Schema(description = "Booking Date", example = "2020-12-01")
  val bookingDate: LocalDate
)

enum class ArrivalType {
  NEW_TO_PRISON
}
