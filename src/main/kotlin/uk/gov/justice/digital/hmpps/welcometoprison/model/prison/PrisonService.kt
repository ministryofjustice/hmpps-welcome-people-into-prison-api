package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.github.javafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.LocationType
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.TemporaryAbsence
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name.properCase
import java.time.LocalDate
import java.time.ZoneId

@Service
class PrisonService(@Autowired private val client: PrisonApiClient, val faker: Faker = Faker()) {

  fun getPrisonerImage(offenderNumber: String): ByteArray? {
    return client.getPrisonerImage(offenderNumber)
  }

  private val numbers = '0'..'9'
  private val letters = 'A'..'Z'
  fun CharRange.get(n: Int) = this.shuffled().take(n).joinToString("")

  fun getPrison(prisonId: String) =
    client.getAgency(prisonId) ?: throw NotFoundException("Could not find prison with id: '$prisonId'")

  fun getTransfers(agencyId: String, date: LocalDate) =
    client.getPrisonTransfersEnRoute(agencyId).map {
      Arrival(
        id = null,
        firstName = properCase(it.firstName),
        lastName = properCase(it.lastName),
        dateOfBirth = it.dateOfBirth,
        fromLocationType = LocationType.PRISON,
        fromLocation = it.fromAgencyDescription,
        prisonNumber = it.offenderNo,
        date = it.movementDate,
        pncNumber = null,
        isCurrentPrisoner = true
      )
    }

  fun getTemporaryAbsences(agencyId: String) = generateSequence {
    TemporaryAbsence(
      firstName = faker.name().firstName(),
      lastName = faker.name().lastName(),
      dateOfBirth = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
      prisonNumber = letters.get(1) + letters.get(4) + numbers.get(2),
      reasonForAbsence = faker.expression("reason")
    )
  }.take((5..20).random()).toList()

  @PreAuthorize("hasRole('BOOKING_CREATE') and hasAuthority('SCOPE_write')")
  fun createAndAdmitOffender(createAndAdmitOffenderDetail: CreateAndAdmitOffenderDetail): CreateOffenderResponse {
    val createResponse = client.createOffender(
      with(createAndAdmitOffenderDetail) {
        CreateOffenderDetail(
          firstName = firstName!!,
          middleName1,
          middleName2,
          lastName = lastName!!,
          dateOfBirth = dateOfBirth!!,
          gender = gender!!,
          ethnicity,
          croNumber,
          pncNumber,
          suffix,
          title
        )
      }
    )

    client.admitOffenderOnNewBooking(
      createResponse.offenderNo,
      with(createAndAdmitOffenderDetail) {
        AdmitOnNewBookingDetail(
          prisonId = prisonId!!,
          bookingInTime,
          fromLocationId,
          movementReasonCode = movementReasonCode!!,
          youthOffender,
          cellLocation,
          imprisonmentStatus = imprisonmentStatus!!
        )
      }
    )
    return CreateOffenderResponse(offenderNo = createResponse.offenderNo)
  }
}
