package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrivals

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "confirmed_arrival")
data class ConfirmedArrival(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val prisonNumber: String,

  val arrivalId: String?,

  val timestamp: LocalDateTime,

  @Enumerated(EnumType.STRING)
  val arrivalType: ConfirmedArrivalType,

  val prisonId: String,

  val bookingId: Long,

  val arrivalDate: LocalDate,

  val username: String?,
)

enum class ConfirmedArrivalType {
  NEW_TO_PRISON,
  NEW_BOOKING_EXISTING_OFFENDER,
  RECALL,
  COURT_TRANSFER,
  TEMPORARY_ABSENCE,
  TRANSFER,
}
