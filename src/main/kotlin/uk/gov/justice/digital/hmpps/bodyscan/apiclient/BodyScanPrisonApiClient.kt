package uk.gov.justice.digital.hmpps.bodyscan.apiclient

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.bodyscan.apiclient.model.PersonalCareCounter
import uk.gov.justice.digital.hmpps.config.typeReference
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class BodyScanPrisonApiClient(@Qualifier("prisonApiWebClient") private val webClient: WebClient) {

  fun getPersonalCareNeedsForPrisonNumbers(
    type: String,
    fromStartDate: LocalDate,
    toStartDate: LocalDate,
    prisonNumbers: List<String>
  ): List<PersonalCareCounter> {
    return webClient.post()
      .uri(
        "/api/bookings/offenderNo/personal-care-needs/count?type=" +
          "$type" +
          "&fromStartDate=${fromStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}" +
          "&toStartDate=${toStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
      )
      .bodyValue(prisonNumbers)
      .retrieve()
      .bodyToMono(typeReference<List<PersonalCareCounter>>())
      .block()
      ?: emptyList()
  }
}
