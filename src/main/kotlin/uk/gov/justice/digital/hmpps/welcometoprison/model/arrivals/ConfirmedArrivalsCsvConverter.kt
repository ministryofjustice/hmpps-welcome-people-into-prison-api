package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.microsoft.applicationinsights.web.dependencies.apachecommons.codec.digest.DigestUtils
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.welcometoprison.model.confirmedarrival.ConfirmedArrival
import java.io.StringWriter
import java.util.stream.Stream

@Component
class ConfirmedArrivalsCsvConverter {

  fun toCsv(events: Stream<ConfirmedArrival>): String {
    val stringWriter = StringWriter()
    val sequenceWriter = csvWriter.writeValues(stringWriter)
    events.forEach { e ->
      val a = e.copy(username = DigestUtils.md5Hex(e.username.orEmpty()))
      sequenceWriter.write(a)
    }
    sequenceWriter.close()
    return stringWriter.toString()
  }

  companion object {

    private val csvWriter: ObjectWriter = CsvMapper()
      .findAndRegisterModules()
      .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
      .configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
      .writer(
        CsvSchema.builder()
          .setUseHeader(true)
          .addColumn("id", CsvSchema.ColumnType.NUMBER)
          .addColumn("timestamp")
          .addColumn("arrivalDate")
          .addColumn("prisonId", CsvSchema.ColumnType.NUMBER)
          .addColumn("arrivalType")
          .addColumn("username")
          .build()
      ).with(JsonGenerator.Feature.IGNORE_UNKNOWN)
  }
}
