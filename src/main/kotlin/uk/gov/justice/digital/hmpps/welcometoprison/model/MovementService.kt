package uk.gov.justice.digital.hmpps.welcometoprison.model

import com.github.javafaker.Faker
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.ZoneId
import java.util.Random

@Service
class MovementService(val faker: Faker = Faker()) {
  private val numbers = '0'..'9'
  private val letters = 'A'..'Z'

  fun CharRange.get(n: Int) = this.shuffled().take(n).joinToString()

  fun getMovements(agencyId: String, date: LocalDate) = Random().ints(5, 15).mapToObj {
    Movement(
      firstName = faker.name().firstName(),
      lastName = faker.name().lastName(),
      dateOfBirth = faker.date().birthday().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
      prisonNumber = letters.get(1) + letters.get(4) + numbers.get(2),
      pncNumber = numbers.get(4) + "/" + numbers.get(7) + letters.get(1),
      date = date,
      moveType = MoveType.values().random()
    )
  }.toList()
}
