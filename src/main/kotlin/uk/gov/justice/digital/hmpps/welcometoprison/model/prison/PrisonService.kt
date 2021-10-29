package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.github.javafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.TemporaryAbsence
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.LocationType
import uk.gov.justice.digital.hmpps.welcometoprison.model.prison.Name.properCase
import java.time.LocalDate
import java.time.ZoneId

@Service
class PrisonService(
  @Autowired private val client: PrisonApiClient,
  val faker: Faker = Faker()
) {

  fun getPrisonerImage(offenderNumber: String): ByteArray? = client.getPrisonerImage(offenderNumber)

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
      prisonNumber = fakePrisonNumber(),
      reasonForAbsence = faker.expression("reason")
    )
  }.take((5..20).random()).toList()

  fun admitOffenderOnNewBooking(
    prisonNumber: String,
    confirmArrivalDetail: ConfirmArrivalDetail
  ) {
    client.admitOffenderOnNewBooking(
      prisonNumber,
      with(confirmArrivalDetail) {
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
  }

  fun createOffender(confirmArrivalDetail: ConfirmArrivalDetail): String =
    client
      .createOffender(
        with(confirmArrivalDetail) {
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
      .offenderNo

  companion object {
    private val numbers = '0'..'9'
    private val letters = 'A'..'Z'

    private operator fun CharRange.invoke(n: Int) = this.shuffled().take(n).joinToString("")
    private fun fakePrisonNumber() = letters(1) + letters(4) + numbers(2)
  }
}
