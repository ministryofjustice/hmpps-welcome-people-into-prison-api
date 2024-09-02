package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.github.tomakehurst.wiremock.client.WireMock
import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.config.ClientException
import uk.gov.justice.digital.hmpps.welcometoprison.integration.PrisonApiMockServer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class PrisonApiClientTest {
  private lateinit var prisonApiClient: PrisonApiClient
  private val telemetryClient: TelemetryClient = mock()

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
    prisonApiClient = PrisonApiClient(webClient, telemetryClient)
  }

  @Test
  fun `get user case loads`() {
    mockServer.stubGetUserCaseLoads()

    val userCaseLoads = prisonApiClient.getUserCaseLoads()

    assertThat(userCaseLoads).containsExactly(
      UserCaseLoad(
        caseLoadId = "MDI",
        description = "Moorland Closed (HMP & YOI)",
      ),
      UserCaseLoad(
        caseLoadId = "NMI",
        description = "Nottingham (HMP)",
      ),
    )

    mockServer.verify(
      WireMock.getRequestedFor(WireMock.urlEqualTo("/api/users/me/caseLoads")),
    )
  }

  @Test
  fun `get prison transfers en-route happy path`() {
    mockServer.stubGetPrisonTransfersEnRoute("NMI", LocalDate.now())

    val offenderMovements = prisonApiClient.getPrisonTransfersEnRoute("NMI", LocalDate.now())

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
        movementDate = LocalDate.of(2011, 9, 8),
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
        movementDate = LocalDate.of(2010, 8, 20),
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
        movementDate = LocalDate.of(2010, 5, 4),
      ),
    )

    mockServer.verify(
      WireMock.getRequestedFor(
        WireMock.urlEqualTo(
          "/api/movements/NMI/enroute?movementDate=" + LocalDate.now().format(
            DateTimeFormatter.ISO_LOCAL_DATE,
          ),
        ),
      ),
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
        gender = "M",
      ),
    )

    assertThat(response.offenderNo).isEqualTo(offenderNumber)
  }

  @Test
  fun `create offender fails`() {
    mockServer.stubCreateOffenderFails(400)
    try {
      prisonApiClient.createOffender(
        CreateOffenderDetail(
          firstName = "A",
          lastName = "Z",
          dateOfBirth = LocalDate.of(1961, 5, 29),
          gender = "M",
        ),
      )
    } catch (exception: Exception) {
    }

    verify(telemetryClient).trackEvent(eq("PrisonApiClientError"), any(), eq(null))
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
        youthOffender = false,
      ),
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
          youthOffender = false,
        ),
      )
    }.isInstanceOf(ClientException::class.java)
  }

  @Test
  fun `admit Offender On New Booking fails with 409 due to UAL`() {
    val offenderNumber = "ABC123A"

    mockServer.stubAdmitOnNewBookingFails(offenderNumber, 409)

    assertThatThrownBy {
      prisonApiClient.admitOffenderOnNewBooking(
        offenderNumber,
        AdmitOnNewBookingDetail(
          prisonId = "NMI",
          imprisonmentStatus = "UAL",
          movementReasonCode = "RECA",
          youthOffender = false,
        ),
      )
    }.isInstanceOf(ClientException::class.java)
  }

  @Test
  fun `getNullByteArrayWhenImageNotExist`() {
    val offenderNumber = "ABC123A"

    mockServer.stubGetImage404(offenderNumber)
    assertThat(prisonApiClient.getPrisonerImage(offenderNumber)).isNull()
  }

  @Test
  fun `getPrisonerImage`() {
    val offenderNumber = "ABC123A"

    mockServer.stubGetImage(offenderNumber)
    var byteArray = prisonApiClient.getPrisonerImage(offenderNumber)
    assertThat(byteArray).isNotEmpty()
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
        youthOffender = false,
      ),
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
          youthOffender = false,
        ),
      )
    }.isInstanceOf(ClientException::class.java)
  }

  @Test
  fun `Transfer in offender`() {
    val offenderNumber = "ABC123A"
    mockServer.stubTransferInOffender(offenderNumber)

    prisonApiClient.transferIn(
      offenderNumber,
      TransferIn(
        cellLocation = "MDI-RECP",
        commentText = "Prisoner was transferred to a new prison",
        receiveTime = LocalDateTime.of(2021, 11, 15, 1, 0, 0),
      ),
    )
  }

  @Test
  fun `Transfer in offender fails`() {
    val offenderNumber = "ABC123A"
    mockServer.stubTransferInOffenderFails(offenderNumber, 404)

    assertThatThrownBy {
      prisonApiClient.transferIn(
        offenderNumber,
        TransferIn(
          cellLocation = "MDI-RECP",
          commentText = "Prisoner was transferred to a new prison",
          receiveTime = LocalDateTime.of(2021, 11, 15, 1, 0, 0),
        ),
      )
    }.isInstanceOf(ClientException::class.java)
  }

  @Test
  fun `Transfer from court successful`() {
    val offenderNumber = "A1234BC"
    mockServer.stubCourtTransferInOffender(offenderNumber)

    prisonApiClient.courtTransferIn(
      offenderNumber,
      CourtTransferIn(
        agencyId = "MDI",
        movementReasonCode = "CA",
        commentText = "Prisoner was transferred from court",
        dateTime = LocalDateTime.of(2021, 11, 15, 1, 0, 0),
      ),
    )
  }

  @Test
  fun `Transfer from Court fails`() {
    val offenderNumber = "A1234BC"
    mockServer.stubCourtTransferInOffenderFails(offenderNumber, 404)

    assertThatThrownBy {
      prisonApiClient.courtTransferIn(
        offenderNumber,
        CourtTransferIn(
          agencyId = "MDI",
          movementReasonCode = "CA",
          commentText = "Prisoner was transferred from court",
          dateTime = LocalDateTime.of(2021, 11, 15, 1, 0, 0),
        ),
      )
    }.isInstanceOf(ClientException::class.java)
  }

  @Test
  fun `Get temporary absences successful`() {
    val agencyId = "MDI"
    val temporaryAbsence = TemporaryAbsence(
      offenderNo = "G1310UO",
      firstName = "EGURZTOF",
      lastName = "TOBONICA",
      dateOfBirth = LocalDate.of(1990, 10, 15),
      movementReason = "Medical/Dental Inpatient Appointment",
      movementReasonCode = "C6",
      toCity = "City",
      toAgency = "ABRYMC",
      toAgencyDescription = "Aberystwyth Magistrates Court",
      movementTime = LocalDateTime.of(2022, 1, 18, 8, 0),
      commentText = "pHnuWeNNnALpHnuWeNNnA",
    )

    mockServer.stubGetTemporaryAbsencesSuccess(agencyId)

    val response = prisonApiClient.getTemporaryAbsences(agencyId)[0]

    assertThat(response).isEqualTo(temporaryAbsence)
  }

  @Test
  fun `Get main offence successful`() {
    val bookingId = 123L
    val mainOffence = MainOffence(
      bookingId = 123,
      offenceDescription = "Burglary dwelling and theft  - no violence",
      offenceCode = "TH68036",
      statuteCode = "TH68",
    )

    mockServer.stubGetMainOffenceSuccess(bookingId)

    val response = prisonApiClient.getMainOffence(bookingId)

    assertThat(response).isEqualTo(mainOffence)
  }

  @Test
  fun `Get empty main offence successful`() {
    val bookingId = 123L
    mockServer.stubGetEmptyMainOffenceSuccess(bookingId)
    val response = prisonApiClient.getMainOffence(bookingId)

    assertThat(response).isNull()
  }

  @Test
  fun `Get main offence when not found`() {
    val bookingId = 123L

    mockServer.stubGetMainOffenceNotFound(bookingId)

    val response = prisonApiClient.getMainOffence(bookingId)

    assertThat(response).isNull()
  }

  @Test
  fun `Get recent movements successful`() {
    val agencyId = "MDI"
    val fromDate = LocalDateTime.of(2020, 1, 18, 8, 0)
    val toDate = LocalDateTime.of(2022, 1, 18, 8, 0)
    mockServer.stubGetMovementSuccess(agencyId, fromDate, toDate)

    val response = prisonApiClient.getMovement(agencyId, fromDate, toDate)

    assertThat(response.size).isEqualTo(2)
  }

  @Test
  fun `Get recent movements successful without location`() {
    val agencyId = "MDI"
    val fromDate = LocalDateTime.of(2020, 1, 18, 8, 0)
    val toDate = LocalDateTime.of(2022, 1, 18, 8, 0)
    mockServer.stubGetMovementSuccessWithNoLocation(agencyId, fromDate, toDate)

    val response = prisonApiClient.getMovement(agencyId, fromDate, toDate)

    assertThat(response.size).isEqualTo(2)
  }

  @Test
  fun `Confirm return from temporary absences successful`() {
    val offenderNumber = "ABC123A"
    mockServer.stubConfirmTemporaryAbsencesSuccess(offenderNumber)

    val response = prisonApiClient.confirmTemporaryAbsencesArrival(
      offenderNumber,
      TemporaryAbsencesArrival(
        agencyId = "NMI",
        movementReasonCode = "ET",
        commentText = "",
        receiveTime = LocalDateTime.of(2021, 11, 15, 1, 0, 0),
      ),
    )
    assertThat(response.offenderNo).isEqualTo(offenderNumber)
  }

  @Test
  fun `Confirm get Agency Test`() {
    val agencyId = "AGE1"
    mockServer.stubGetAgencySuccess(agencyId)
    assertThat(prisonApiClient.getAgency(agencyId)).isNotNull
  }
}
