package uk.gov.justice.digital.hmpps.welcometoprison.model

import com.github.javafaker.Faker
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

@Service
class PrisonService(val faker: Faker = Faker()) {
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
