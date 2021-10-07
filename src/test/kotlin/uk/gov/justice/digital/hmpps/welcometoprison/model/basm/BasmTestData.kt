package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Location
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MoveType.PRISON_REMAND
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MoveType.PRISON_TRANSFER
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Movement
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.People
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Profile
import java.time.LocalDate

class BasmTestData {

  companion object {

    val PRISON = Location(
      "a2bc2abf-75fe-4b7f-bf5a-a755bc290757",
      "prison",
      "NOTTINGHAM (HMP)",
      "NMI"
    )

    val MOVEMENTS = listOf(
      Movement(
        id = "476d47a3-013a-4772-94c7-5d043b0d0574",
        type = "moves",
        additional_information = "example Court to Prison prison_remand: Huddersfield Youth Court to HMP Isle of Wight",
        date = LocalDate.of(2021, 9, 29),
        date_from = null,
        date_to = null,
        move_type = PRISON_REMAND,
        reference = "MUT4738J",
        status = "completed",
        time_due = "2021-09-29T09:05:09+01:00",
        created_at = "2021-09-29T09:05:09+01:00",
        updated_at = "2021-09-29T09:05:10+01:00",
        from_location = Location(
          id = "6c1047cf-c8e8-4034-9899-d05ac1b07038",
          title = "Penrith County Court",
          location_type = "court",
          nomis_agency_id = "PENRCT"
        ),
        to_location = Location(
          id = "a2bc2abf-75fe-4b7f-bf5a-a755bc290757",
          title = "NOTTINGHAM (HMP)",
          location_type = "prison",
          nomis_agency_id = "NMI"
        ),
        profile = Profile(
          id = "45a2b4a8-38ec-46f4-b882-148a21ebbe6e",
          person = People(
            id = "bd1bf67e-d160-4032-ad59-8f62cd7b25fe",
            first_names = "Alexis",
            last_name = "Jones",
            date_of_birth = LocalDate.of(1996, 7, 23),
            prison_number = null,
            criminal_records_office = null,
            police_national_computer = null
          )
        )
      ),

      Movement(
        id = "0cb56df1-f421-44a9-9f4d-0b4b5661b29f",
        type = "moves",
        additional_information = "test",
        date = LocalDate.of(2021, 4, 29),
        date_from = LocalDate.of(2021, 4, 28),
        date_to = null,
        move_type = PRISON_TRANSFER,
        reference = "ASR8152M",
        status = "completed",
        time_due = null,
        created_at = "2021-04-27T16:20:40+01:00",
        updated_at = "2021-04-28T15:01:03+01:00",
        from_location = Location(
          id = "dc76e71d-413b-48c8-b1cf-85c702c5f465",
          title = "BELMARSH (HMP)",
          location_type = "prison",
          nomis_agency_id = "BAI"
        ),
        to_location = Location(
          id = "efee92c0-d804-4287-8c84-07f98b7dada1",
          title = "WANDSWORTH (HMP)",
          location_type = "prison",
          nomis_agency_id = "WWI"
        ),
        profile = Profile(
          id = "0bb12c9e-69b3-4215-b171-05f0ac9eaaba",
          person = People(
            id = "279ab703-1baa-4bae-b6b9-c8e01675d620",
            first_names = "JIM",
            last_name = "SMITH",
            date_of_birth = LocalDate.of(1991, 7, 31),
            prison_number = "A1234AA",
            criminal_records_office = "123456/96M",
            police_national_computer = "99/123456J"
          )
        )
      ),
    )
  }
}
