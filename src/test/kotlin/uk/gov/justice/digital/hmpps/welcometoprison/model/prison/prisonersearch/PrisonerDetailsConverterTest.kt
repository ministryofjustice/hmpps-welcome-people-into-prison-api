package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.CURRENTLY_IN
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.FROM_COURT
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.FROM_TEMPORARY_ABSENCE
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.NEW_BOOKING
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.ArrivalType.UNKNOWN
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.prisonersearch.response.MatchPrisonerResponse
import java.time.LocalDate

class PrisonerDetailsConverterTest {

  fun whenever(status: String, moveType: String = "") = MatchPrisonerResponse(
    firstName = "FIRST_NAME",
    lastName = "LAST_NAME",
    dateOfBirth = LocalDate.of(2000, 1, 1),
    prisonerNumber = "PRISON_NUMBER",
    pncNumber = "PNC_NUMBER",
    croNumber = "CRO_NUMBER",
    gender = "SEX",
    status = status,
    prisonId = "MDI",
    lastMovementTypeCode = moveType
  )

  @Nested
  @DisplayName("Calculate arrival type")
  inner class CalculateArrivalType {

    @Test
    fun `unknown status`() {
      whenever(status = "something random") `then arrival type is` UNKNOWN
    }

    @Test
    fun `unknown movement type`() {
      whenever(status = "ACTIVE OUT", moveType = "unknown move type") `then arrival type is` UNKNOWN
    }

    @Test
    fun transfer() {
      whenever(status = "INACTIVE TRN") `then arrival type is` TRANSFER
    }

    @Test
    fun `new booking`() {
      whenever(status = "INACTIVE OUT") `then arrival type is` NEW_BOOKING
    }

    @Test
    fun `currently in`() {
      whenever(status = "ACTIVE IN") `then arrival type is` CURRENTLY_IN
    }

    @Test
    fun `from temporary absence`() {
      whenever(status = "ACTIVE OUT", moveType = "TAP") `then arrival type is` FROM_TEMPORARY_ABSENCE
    }

    @Test
    fun `from court`() {
      whenever(status = "ACTIVE OUT", moveType = "CRT") `then arrival type is` FROM_COURT
    }
  }

  private infix fun MatchPrisonerResponse.`then arrival type is`(type: ArrivalType) {
    assertThat(this.calculateArrivalType()).isEqualTo(type)
  }
}
