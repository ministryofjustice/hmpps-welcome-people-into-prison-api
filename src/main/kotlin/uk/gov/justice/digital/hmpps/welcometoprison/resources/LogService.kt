package uk.gov.justice.digital.hmpps.welcometoprison.resources

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LogService {
  fun generateLog(type: String, random: Int): String {
    try {
      if ("INFO" == type.uppercase()) {
        log.info("LogService info message: $random")
      } else if ("WARNING" == type.uppercase()) {
        log.warn("LogService warning message: $random")
      } else {
        log.error("LogService error message: $random")
      }
    } catch (exception: Exception) {
      log.error("LogService exception", exception)
    }
    return "ok"
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
