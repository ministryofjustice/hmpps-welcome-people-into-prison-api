package uk.gov.justice.digital.hmpps

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication()
class App

const val SYSTEM_USERNAME = "WELCOME_PEOPLE_INTO_PRISON_API"

fun main(args: Array<String>) {
  runApplication<App>(*args)
}
