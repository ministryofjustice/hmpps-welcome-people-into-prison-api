package uk.gov.justice.digital.hmpps.welcometoprison

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class WelcomePeopleToPrisonApi

fun main(args: Array<String>) {
  runApplication<WelcomePeopleToPrisonApi>(*args)
}
