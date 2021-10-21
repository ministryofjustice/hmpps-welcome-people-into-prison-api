package uk.gov.justice.digital.hmpps.welcometoprison.model

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.BasmService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.PrisonerSearchService
import uk.gov.justice.digital.hmpps.welcometoprison.model.prisonersearch.response.MatchPrisonerResponse
import uk.gov.justice.digital.hmpps.welcometoprison.repository.BookingRepository
import java.time.LocalDate

@Service
class ArrivalsService(
  val basmService: BasmService,
  val prisonService: PrisonService,
  val prisonerSearchService: PrisonerSearchService,
  val bookingRepository: BookingRepository
) {
  fun getMovement(moveId: String): Arrival = addPrisonData(basmService.getArrival(moveId))

  fun getMovements(agencyId: String, date: LocalDate): List<Arrival> {
    val arrivals = basmService.getArrivals(agencyId, date, date).map { addPrisonData(it) } + prisonService.getTransfers(agencyId, date)
    val bookings = bookingRepository.findAllByBookingDateAndPrisonId(date, agencyId)

    return arrivals.filterNot { contains(it, bookings, date) }
  }

  fun contains(arrival: Arrival, bookings: List<Booking>, bookingDate: LocalDate) = bookings.stream().anyMatch {
    it.bookingDate == bookingDate &&
    arrival.id == it.movementId &&
    arrival.prisonNumber == it.prisonId
    arrival.moveType == it.moveType
  }

  private fun addPrisonData(movement: Arrival): Arrival {
    val candidates = prisonerSearchService.getCandidateMatches(movement)

    val match = candidates.firstOrNull { movement.isMatch(it) }

    return if (match != null)
      movement.copy(pncNumber = match.pncNumber, prisonNumber = match.prisonerNumber)

    // Ignore any provided prison number, but assume PNC is fine
    else
      movement.copy(prisonNumber = null)
  }

  companion object {
    fun Arrival.isMatch(move: MatchPrisonerResponse) = when {
      prisonNumber != null && pncNumber != null ->
        prisonNumber == move.prisonerNumber && pncNumber == move.pncNumber

      prisonNumber != null && pncNumber == null ->
        prisonNumber == move.prisonerNumber

      prisonNumber == null && pncNumber != null ->
        pncNumber == move.pncNumber

      else -> false
    }
  }
}
