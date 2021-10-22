package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.LocationType
import java.time.LocalDate
import java.time.LocalTime

class PrisonServiceTest {

  private val client = mock<PrisonApiClient>()

  private val service = PrisonService(client)

  private val prisonImage = "prisonImage".toByteArray()

  @Test
  fun `get transfers`() {
    whenever(client.getPrisonTransfersEnRoute(any())).thenReturn(
      listOf(
        OffenderMovement(
          offenderNo = "G6081VQ",
          bookingId = 472195,
          dateOfBirth = LocalDate.of(1981, 7, 4),
          firstName = "IRUNCEKAS",
          lastName = "BRONSERIA",
          fromAgency = "DNI",
          fromAgencyDescription = "Doncaster (HMP)",
          toAgency = "NMI",
          toAgencyDescription = "Nottingham (HMP)",
          movementType = "PRISON_REMAND",
          movementTypeDescription = "Transfers",
          movementReason = "NOTR",
          movementReasonDescription = "Normal Transfer",
          directionCode = "OUT",
          movementTime = LocalTime.of(12, 0, 0),
          movementDate = LocalDate.of(2011, 9, 8)
        ),
      )
    )

    val moves = service.getTransfers("NMI", LocalDate.now())

    assertThat(moves).containsExactly(
      Arrival(
        id = null,
        prisonNumber = "G6081VQ",
        dateOfBirth = LocalDate.of(1981, 7, 4),
        firstName = "Iruncekas",
        lastName = "Bronseria",
        fromLocation = "Doncaster (HMP)",
        fromLocationType = LocationType.PRISON,
        date = LocalDate.of(2011, 9, 8),
        pncNumber = null,
        moveType = "PRISON_REMAND"
      )
    )

    verify(client).getPrisonTransfersEnRoute("NMI")
  }

  @Test
  fun `gets prisoner image`() {
    whenever(client.getPrisonerImage(any())).thenReturn(prisonImage)

    val result = service.getPrisonerImage("A12345")

    assertThat(result).isEqualTo(prisonImage)
  }

  @Test
  fun `Create and admit offender`() {
    val offenderNo = "A1111AA"

    whenever(client.createOffender(any())).thenReturn(CreateOffenderResponse(offenderNo))

    val response = service.createAndAdmitOffender(
      CreateAndAdmitOffenderDetail(
        firstName = "Alpha",
        lastName = "Omega",
        dateOfBirth = LocalDate.of(1961, 5, 29),
        gender = "M",
        prisonId = "NMI",
        movementReasonCode = "N",
        imprisonmentStatus = "SENT03"
      )
    )

    assertThat(response.offenderNo).isEqualTo(offenderNo)

    verify(client).createOffender(
      CreateOffenderDetail(
        firstName = "Alpha",
        lastName = "Omega",
        dateOfBirth = LocalDate.of(1961, 5, 29),
        gender = "M"
      )
    )

    verify(client).admitOffenderOnNewBooking(
      offenderNo,
      AdmitOnNewBookingDetail(
        prisonId = "NMI",
        movementReasonCode = "N",
        imprisonmentStatus = "SENT03"
      )
    )
  }
}
