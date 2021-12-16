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
import javax.persistence.Table

@Schema(description = "Booking made by the service")
@Entity
@Table(name = "confirmed_arrival")
class ConfirmedArrival(
  @Id
  @Schema(description = "ID", example = "")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long?,

  @Schema(description = "Prison Number", example = "A1234AA")
  val prisonNumber: String,

  @Schema(description = "Movement Id", example = "123e4567-e89b-12d3-a456-426614174000")
  val movementId: String,

  @Schema(description = "Timestamp")
  val timestamp: LocalDateTime,

  @Schema(description = "Arrival Type")
  @Enumerated(EnumType.STRING)
  val arrivalType: ArrivalType,

  @Schema(description = "Prison Id", example = "MDI")
  val prisonId: String,

  @Schema(description = "Booking Id", example = "123")
  val bookingId: Long,

  @Schema(description = "Arrival Date", example = "2020-12-01")
  val arrivalDate: LocalDate
)

enum class ArrivalType {
  NEW_TO_PRISON,
  NEW_BOOKING_EXISTING_OFFENDER,
  RECALL,
  COURT_TRANSFER
}
