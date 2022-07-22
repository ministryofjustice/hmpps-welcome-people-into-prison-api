package uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival

import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "confirmed_arrival")
data class ConfirmedArrival(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  val prisonNumber: String,

  val movementId: String?,

  val timestamp: LocalDateTime,

  @Enumerated(EnumType.STRING)
  val arrivalType: ArrivalType,

  val prisonId: String,

  val bookingId: Long,

  val arrivalDate: LocalDate,

  val username: String
)

enum class ArrivalType {
  NEW_TO_PRISON,
  NEW_BOOKING_EXISTING_OFFENDER,
  RECALL,
  COURT_TRANSFER,
  TEMPORARY_ABSENCE,
  TRANSFER
}
