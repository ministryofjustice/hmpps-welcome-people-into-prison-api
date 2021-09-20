package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import com.github.javafaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.welcometoprison.exception.NotFoundException
import uk.gov.justice.digital.hmpps.welcometoprison.model.MoveType
import uk.gov.justice.digital.hmpps.welcometoprison.model.Movement
import java.time.LocalDate
import java.time.ZoneId
import java.util.Random

@Service
class PrisonService(@Autowired private val client: PrisonApiClient, val faker: Faker = Faker()) {

  fun getPrisonerImage(offenderNumber: String): ByteArray? {
    val prisonerImage: PrisonerImage? =
      client.getPrisonerImages(offenderNumber)
        .filter { it.imageView.equals("FACE") && it.imageOrientation.equals("FRONT") }
        .maxByOrNull { it.captureDate }

    return prisonerImage?.let { client.getPrisonerImage(it.imageId) }
      ?: throw NotFoundException("No front-facing image of the offenders face found for offender number: $offenderNumber")
  }

  private val numbers = '0'..'9'
  private val letters = 'A'..'Z'
  fun CharRange.get(n: Int) = this.shuffled().take(n).joinToString("")

  fun getMoves(agencyId: String, date: LocalDate) = generateSequence {
    Movement(
      firstName = faker.name().firstName(),
      lastName = faker.name().lastName(),
      dateOfBirth = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
      fromLocation = faker.address().city(),
      prisonNumber = if (Random().nextBoolean()) letters.get(1) + letters.get(4) + numbers.get(2) else null,
      pncNumber = numbers.get(4) + "/" + numbers.get(7) + letters.get(1),
      date = date,
      moveType = MoveType.PRISON_TRANSFER,
    )
  }.take((5..20).random()).toList()
}
