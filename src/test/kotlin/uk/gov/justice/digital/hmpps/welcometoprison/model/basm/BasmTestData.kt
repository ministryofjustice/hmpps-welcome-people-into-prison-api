package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.IncludesRelationships
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Location
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.LocationAttributes
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MoveType.PRISON_TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MovementAttributes
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MovementResponseWrapper
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.People
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.PeopleAttributes
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Profile
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Relationship
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.RelationshipData
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Relationships
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.ResponseMetadata
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.ResponsePagination
import java.time.LocalDate

class BasmTestData {

  companion object {

    val PRISON = Location(
      "a2bc2abf-75fe-4b7f-bf5a-a755bc290757",
      LocationAttributes("nmi", "NOTTINGHAM (HMP)", "prison", "NMI")
    )

    val MOVEMENTS = MovementResponseWrapper(
      data = listOf(
        Movement(
          id = "0cb56df1-f421-44a9-9f4d-0b4b5661b29f",
          type = "moves",
          attributes = MovementAttributes(
            additional_information = "test",
            date = LocalDate.of(2021, 4, 29),
            date_from = LocalDate.of(2021, 4, 28),
            date_to = null,
            move_type = PRISON_TRANSFER,
            reference = "ASR8152M",
            status = "completed",
            time_due = null,
            created_at = "2021-04-27T16:20:40+01:00",
            updated_at = "2021-04-28T15:01:03+01:00"
          ),
          relationships = Relationships(
            from_location = Relationship(
              data = RelationshipData(
                id = "dc76e71d-413b-48c8-b1cf-85c702c5f465",
                type = "locations"
              )
            ),
            to_location = Relationship(
              data = RelationshipData(
                id = "efee92c0-d804-4287-8c84-07f98b7dada1",
                type = "locations"
              )
            ),
            profile = Relationship(
              data = RelationshipData(
                id = "0bb12c9e-69b3-4215-b171-05f0ac9eaaba",
                type = "profiles"
              )
            ),
            supplier = Relationship(
              data = RelationshipData(
                id = "3ef88a47-6f1f-5b9b-b2fc-c0fe42cb0c92",
                type = "suppliers"
              )
            )
          )
        )
      ),
      meta = ResponseMetadata(pagination = ResponsePagination(per_page = 1, total_pages = 76, total_objects = 76)),
      included = listOf(
        People(
          id = "279ab703-1baa-4bae-b6b9-c8e01675d620",
          attributes = PeopleAttributes(
            first_names = "JIM",
            last_name = "SMITH",
            date_of_birth = LocalDate.of(1991, 7, 31),
            prison_number = "A1234AA",
            criminal_records_office = "123456/96M",
            police_national_computer = "99/123456J"
          ),
          relationships = IncludesRelationships(person = null)
        ),
        Profile(
          id = "0bb12c9e-69b3-4215-b171-05f0ac9eaaba",
          relationships = IncludesRelationships(
            person = Relationship(
              data = RelationshipData(
                id = "279ab703-1baa-4bae-b6b9-c8e01675d620",
                type = "people"
              )
            )
          )
        ),
        Location(
          id = "dc76e71d-413b-48c8-b1cf-85c702c5f465",
          attributes = LocationAttributes(
            key = "bai",
            title = "BELMARSH (HMP)",
            location_type = "prison",
            nomis_agency_id = "BAI"
          )
        ),
        Location(
          id = "efee92c0-d804-4287-8c84-07f98b7dada1",
          attributes = LocationAttributes(
            key = "wwi",
            title = "WANDSWORTH (HMP)",
            location_type = "prison",
            nomis_agency_id = "WWI"
          )
        )
      )
    )
  }
}
