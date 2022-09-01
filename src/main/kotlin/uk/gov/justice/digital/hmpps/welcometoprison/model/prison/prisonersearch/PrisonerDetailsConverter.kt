package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals.PotentialMatch
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.PrisonerDetails
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.CURRENTLY_IN
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.FROM_COURT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.FROM_TEMPORARY_ABSENCE
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.NEW_BOOKING
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.UNKNOWN
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse

enum class ArrivalType {
  NEW_BOOKING,
  TRANSFER,
  FROM_TEMPORARY_ABSENCE,
  FROM_COURT,

  // The following shouldn't really happen
  CURRENTLY_IN,
  UNKNOWN
}

fun MatchPrisonerResponse.toPrisonerDetails() = PrisonerDetails(
  firstName = Name.properCase(this.firstName),
  lastName = Name.properCase(this.lastName),
  dateOfBirth = this.dateOfBirth,
  prisonNumber = this.prisonerNumber,
  pncNumber = this.pncNumber,
  croNumber = this.croNumber,
  isCurrentPrisoner = this.isCurrentPrisoner,
  sex = this.gender,
  arrivalType = this.calculateArrivalType(),
  arrivalTypeDescription = this.buildArrivalDescription()
)

fun MatchPrisonerResponse.toPotentialMatch() = PotentialMatch(
  firstName = Name.properCase(this.firstName),
  lastName = Name.properCase(this.lastName),
  dateOfBirth = this.dateOfBirth,
  prisonNumber = this.prisonerNumber,
  pncNumber = this.pncNumber,
  croNumber = this.croNumber,
  isCurrentPrisoner = this.isCurrentPrisoner,
  sex = this.gender,
  arrivalType = this.calculateArrivalType(),
  arrivalTypeDescription = this.buildArrivalDescription()
)

private fun MatchPrisonerResponse.buildArrivalDescription() = "$status-$lastMovementTypeCode-$prisonId"

fun MatchPrisonerResponse.calculateArrivalType() = when (status) {
  "INACTIVE TRN" -> TRANSFER
  "INACTIVE OUT" -> NEW_BOOKING
  "ACTIVE IN" -> CURRENTLY_IN
  "ACTIVE OUT" -> when (lastMovementTypeCode) {
    "TAP" -> FROM_TEMPORARY_ABSENCE
    "CRT" -> FROM_COURT
    else -> UNKNOWN
  }
  else -> UNKNOWN
}
