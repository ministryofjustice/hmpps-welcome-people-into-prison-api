package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.github.javafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.model.LocationType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.TemporaryAbsence
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

  fun getMoves(agencyId: String, date: LocalDate) = generateSequence {
    Movement(
      id = null,
      firstName = faker.name().firstName(),
      lastName = faker.name().lastName(),
      dateOfBirth = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
      fromLocation = faker.address().city(),
      prisonNumber = letters.get(1) + numbers.get(4) + letters.get(2),
      pncNumber = numbers.get(4) + "/" + numbers.get(7) + letters.get(1),
      date = date,
      fromLocationType = LocationType.PRISON,
    )
  }.take((5..20).random()).toList()

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
