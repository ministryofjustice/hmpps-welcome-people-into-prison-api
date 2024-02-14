package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.JsonApiQueryBuilder.Order
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.JsonApiQueryBuilder.queryOf
import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8

class JsonApiQueryBuilderTest {

  @Test
  fun `empty query`() =
    queryOf() `decodes to` ""

  @Test
  fun `includes only`() =
    queryOf(includes = listOf("profile.person", "profile.person_escort_record.flags")) `decodes to`
      "?include=profile.person,profile.person_escort_record.flags"

  @Test
  fun `single filter`() =
    queryOf(filters = mapOf("name" to listOf("Jim"))) `decodes to` "?filter[name]=Jim"

  @Test
  fun `multiple filters`() =
    queryOf(
      filters = mapOf(
        "name" to listOf("Jim"),
        "age" to listOf("65"),
      ),
    ) `decodes to` "?filter[name]=Jim&filter[age]=65"

  @Test
  fun `includes and filters`() =
    queryOf(
      filters = mapOf("name" to listOf("Jim"), "age" to listOf("65")),
      includes = listOf("profile.person", "profile.person_escort_record.flags"),
    ) `decodes to` "?include=profile.person,profile.person_escort_record.flags&filter[name]=Jim&filter[age]=65"

  @Test
  fun `check pagination`() =
    queryOf(
      page = 3,
      perPage = 20,
    ) `decodes to` "?page=3&per_page=20"

  @Test
  fun `check sorting`() =
    queryOf(
      sort = "name" to Order.ASC,
    ) `decodes to` "?sort[by]=name&sort[direction]=asc"

  @Test
  fun `complex example`() =
    queryOf(
      filters = mapOf(
        "to_location_id" to listOf("123"),
        "date_from" to listOf("2020-02-12"),
        "date_to" to listOf("2020-02-13"),
        "status" to listOf("requested", "accepted", "booked", "in_transit", "completed"),
      ),
      sort = "date" to Order.ASC,
      page = 1,
      perPage = 200,
      includes = listOf("profile.person", "profile.person_escort_record.flags"),
    ) `decodes to` "?include=profile.person,profile.person_escort_record.flags&filter[to_location_id]=123&filter[date_from]=2020-02-12&filter[date_to]=2020-02-13&filter[status]=requested,accepted,booked,in_transit,completed&page=1&per_page=200&sort[by]=date&sort[direction]=asc"
}

infix fun String.`decodes to`(expected: String) =
  Assertions.assertEquals(expected, URLDecoder.decode(this, UTF_8))
