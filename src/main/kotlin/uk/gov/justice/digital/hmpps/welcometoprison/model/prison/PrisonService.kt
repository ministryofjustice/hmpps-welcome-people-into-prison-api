package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.github.javafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.LocationType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
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

  fun getMoves(agencyId: String, date: LocalDate) =
    client.getPrisonTransfersEnRoute(agencyId).map {
      Movement(
        id = null,
        firstName = properCase(it.firstName),
        lastName = properCase(it.lastName),
        dateOfBirth = it.dateOfBirth,
        fromLocationType = LocationType.PRISON,
        fromLocation = it.fromAgencyDescription,
        prisonNumber = it.offenderNo,
        date = it.movementDate,
        pncNumber = null
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
}
