package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.JsonApiQueryBuilder.Order
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.JsonApiQueryBuilder.`query of`
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

class JsonApiQueryBuilderTest {

  @Test
  fun `empty query`() =
    `query of`() `decodes to` ""

  @Test
  fun `includes only`() =
    `query of`(includes = listOf("profile.person", "profile.person_escort_record.flags")) `decodes to`
      "?include=profile.person,profile.person_escort_record.flags"

  @Test
  fun `single filter`() =
    `query of`(filters = mapOf("name" to listOf("Jim"))) `decodes to` "?filter[name]=Jim"

  @Test
  fun `multiple filters`() =
    `query of`(
      filters = mapOf(
        "name" to listOf("Jim"),
        "age" to listOf("65")
      )
    ) `decodes to` "?filter[name]=Jim&filter[age]=65"

  @Test
  fun `includes and filters`() =
    `query of`(
      filters = mapOf("name" to listOf("Jim"), "age" to listOf("65")),
      includes = listOf("profile.person", "profile.person_escort_record.flags")
    ) `decodes to` "?include=profile.person,profile.person_escort_record.flags&filter[name]=Jim&filter[age]=65"

  @Test
  fun `parameter encoding`() =
    `query of`(
      filters = mapOf("name" to listOf("Jim"), "age" to listOf("65")),
      includes = listOf("education", "transport")
    ) `encodes to` "?include=education%2Ctransport&filter%5Bname%5D=Jim&filter%5Bage%5D=65"

  @Test
  fun `check pagination`() =
    `query of`(
      page = 3,
      perPage = 20
    ) `decodes to` "?page=3&per_page=20"

  @Test
  fun `check sorting`() =
    `query of`(
      sort = "name" to Order.asc
    ) `decodes to` "?sort[by]=name&sort[direction]=asc"

  @Test
  fun `complex example`() =
    `query of`(
      filters = mapOf(
        "to_location_id" to listOf("123"),
        "date_from" to listOf("2020-02-12"),
        "date_to" to listOf("2020-02-13"),
        "status" to listOf("requested", "accepted", "booked", "in_transit", "completed")
      ),
      sort = "date" to Order.asc,
      page = 1,
      perPage = 200,
      includes = listOf("profile.person", "profile.person_escort_record.flags")
    ) `decodes to` "?include=profile.person,profile.person_escort_record.flags&filter[to_location_id]=123&filter[date_from]=2020-02-12&filter[date_to]=2020-02-13&filter[status]=requested,accepted,booked,in_transit,completed&page=1&per_page=200&sort[by]=date&sort[direction]=asc"
}

infix fun String.`decodes to`(expected: String) =
  Assertions.assertEquals(expected, URLDecoder.decode(this, UTF_8))

infix fun String.`encodes to`(expected: String) =
  Assertions.assertEquals(expected, this)
