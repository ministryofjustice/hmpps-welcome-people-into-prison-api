package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.github.tomakehurst.wiremock.client.WireMock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.welcometoprison.integration.PrisonApiMockServer
import java.time.LocalDate
import java.time.LocalTime

class PrisonApiClientTest {
  private lateinit var prisonApiClient: PrisonApiClient

  companion object {
    @JvmField
    internal val mockServer = PrisonApiMockServer()

    @BeforeAll
    @JvmStatic
    fun startMocks() {
      mockServer.start()
    }

    @AfterAll
    @JvmStatic
    fun stopMocks() {
      mockServer.stop()
    }
  }

  @BeforeEach
  fun resetStubs() {
    mockServer.resetAll()
    val webClient = WebClient.create("http://localhost:${mockServer.port()}")
    prisonApiClient = PrisonApiClient(webClient)
  }

  @Test
  fun `get prison transfers en-route happy path`() {
    mockServer.stubGetPrisonTransfersEnRoute("NMI")

    val offenderMovements = prisonApiClient.getPrisonTransfersEnRoute("NMI")

    assertThat(offenderMovements).containsExactly(
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
        movementType = "TRN",
        movementTypeDescription = "Transfers",
        movementReason = "NOTR",
        movementReasonDescription = "Normal Transfer",
        directionCode = "OUT",
        movementTime = LocalTime.of(12, 0, 0),
        movementDate = LocalDate.of(2011, 9, 8)
      ),
      OffenderMovement(
        offenderNo = "G1038GO",
        bookingId = 187309,
        dateOfBirth = LocalDate.of(1983, 6, 6),
        firstName = "ODNAIRAB",
        lastName = "TIARYON",
        fromAgency = "DGI",
        fromAgencyDescription = "Dovegate (HMP)",
        toAgency = "NMI",
        toAgencyDescription = "Nottingham (HMP)",
        movementType = "TRN",
        movementTypeDescription = "Transfers",
        movementReason = "NOTR",
        movementReasonDescription = "Normal Transfer",
        directionCode = "OUT",
        movementTime = LocalTime.of(14, 0, 0),
        movementDate = LocalDate.of(2010, 8, 20)
      ),
      OffenderMovement(
        offenderNo = "G5428GJ",
        bookingId = 133681,
        dateOfBirth = LocalDate.of(1980, 11, 1),
        firstName = "A'LESSAHIM",
        lastName = "ALANOINE",
        fromAgency = "DNI",
        fromAgencyDescription = "Doncaster (HMP)",
        toAgency = "NMI",
        toAgencyDescription = "Nottingham (HMP)",
        movementType = "TRN",
        movementTypeDescription = "Transfers",
        movementReason = "NOTR",
        movementReasonDescription = "Normal Transfer",
        directionCode = "OUT",
        movementTime = LocalTime.of(12, 0, 0),
        movementDate = LocalDate.of(2010, 5, 4)
      )
    )

    mockServer.verify(
      WireMock.getRequestedFor(WireMock.urlEqualTo("/api/movements/NMI/enroute"))
    )
  }

  @Test
  fun `create offender`() {
    val offenderNumber = "ABC123A"

    mockServer.stubCreateOffender(offenderNumber)

    val response = prisonApiClient.createOffender(
      CreateOffenderDetail(
        firstName = "A",
        lastName = "Z",
        dateOfBirth = LocalDate.of(1961, 5, 29),
        gender = "M"
      )
    )

    assertThat(response.offenderNo).isEqualTo(offenderNumber)
  }

  @Test
  fun `admit Offender On New Booking`() {
    val offenderNumber = "ABC123A"

    mockServer.stubAdmitOnNewBooking(offenderNumber)

    prisonApiClient.admitOffenderOnNewBooking(
      offenderNumber,
      AdmitOnNewBookingDetail(
        prisonId = "NMI",
        imprisonmentStatus = "SENT03",
        movementReasonCode = "C",
        youthOffender = false
      )
    )
  }

  @Test
  fun `admit Offender On New Booking fails`() {
    val offenderNumber = "ABC123A"

    mockServer.stubAdmitOnNewBookingFails(offenderNumber, 404)

    assertThatThrownBy {
      prisonApiClient.admitOffenderOnNewBooking(
        offenderNumber,
        AdmitOnNewBookingDetail(
          prisonId = "NMI",
          imprisonmentStatus = "SENT03",
          movementReasonCode = "C",
          youthOffender = false
        )
      )
    }.isInstanceOf(WebClientResponseException::class.java)
  }

  @Test
  fun `Recall offender`() {
    val offenderNumber = "ABC123A"

    mockServer.stubRecallOffender(offenderNumber)

    prisonApiClient.recallOffender(
      offenderNumber,
      RecallBooking(
        prisonId = "NMI",
        imprisonmentStatus = "SENT03",
        movementReasonCode = "C",
        youthOffender = false
      )
    )
  }

  @Test
  fun `Recall offender fails`() {
    val offenderNumber = "ABC123A"

    mockServer.stubRecallOffenderFails(offenderNumber, 404)

    assertThatThrownBy {
      prisonApiClient.recallOffender(
        offenderNumber,
        RecallBooking(
          prisonId = "NMI",
          imprisonmentStatus = "SENT03",
          movementReasonCode = "C",
          youthOffender = false
        )
      )
    }.isInstanceOf(WebClientResponseException::class.java)
  }
}
