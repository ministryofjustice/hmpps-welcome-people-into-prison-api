package uk.gov.justice.digital.hmpps.welcometoprison.model.basm

import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Gender
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.Location
import uk.gov.justice.digital.hmpps.welcometoprison.model.basm.Model.MoveType.COURT_APPEARANCE
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
      "NMI",
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
          nomis_agency_id = "PENRCT",
        ),
        to_location = Location(
          id = "a2bc2abf-75fe-4b7f-bf5a-a755bc290757",
          title = "NOTTINGHAM (HMP)",
          location_type = "prison",
          nomis_agency_id = "NMI",
        ),
        profile = Profile(
          id = "45a2b4a8-38ec-46f4-b882-148a21ebbe6e",
          person = People(
            id = "bd1bf67e-d160-4032-ad59-8f62cd7b25fe",
            first_names = "Alexis robert",
            last_name = "Jones",
            date_of_birth = LocalDate.of(1996, 7, 23),
            prison_number = null,
            criminal_records_office = null,
            police_national_computer = null,
            gender = Gender.MALE,
          ),
          person_escort_record = null,
        ),
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
          nomis_agency_id = "BAI",
        ),
        to_location = Location(
          id = "efee92c0-d804-4287-8c84-07f98b7dada1",
          title = "WANDSWORTH (HMP)",
          location_type = "prison",
          nomis_agency_id = "WWI",
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
            police_national_computer = "99/123456J",
            gender = null,
          ),
          person_escort_record = null,
        ),
      ),
    )

    val MOVEMENT = Movement(
      id = "6052fac2-ea13-408c-9786-02d0dc5e89ff",
      type = "moves",
      additional_information = null,
      date = LocalDate.of(2021, 9, 22),
      date_from = null,
      date_to = null,
      move_type = COURT_APPEARANCE,
      reference = "MJE8756Y",
      status = "requested",
      time_due = null,
      created_at = "2021-09-22T15:02:38+01:00",
      updated_at = "2021-09-22T15:02:39+01:00",
      from_location = Location(
        id = "fecfd195-21af-4cbc-8ad9-3f8d0345d478",
        title = "Moorland (HMP & YOI)",
        location_type = "prison",
        nomis_agency_id = "MDI",
      ),
      to_location = Location(
        id = "2bffa952-398d-4593-b895-8b9f8492cd7a",
        title = "Barnstaple Magistrates Court",
        location_type = "court",
        nomis_agency_id = "BNSTMC",
      ),
      profile = Profile(
        id = "8e4ff6e6-0d4f-49e2-aff6-f3022501be31",
        person = People(
          id = "0482f85c-2216-40ca-a73d-67fcd2ae283f",
          first_names = "JASON harry",
          last_name = "SIMS",
          date_of_birth = LocalDate.of(1984, 1, 24),
          prison_number = null,
          criminal_records_office = null,
          police_national_computer = null,
          null,
        ),
        person_escort_record = Model.PersonEscortRecords(
          id = "06e826ce-a0bf-44c9-993d-093eb16993c9",
          responses = null,
        ),
      ),
    )

    val MOVEMENT_WITH_OFFENCE = Movement(
      id = "eb3c5901-3e8f-4f0c-a1e5-d1ae52b886c4",
      type = "moves",
      additional_information = null,
      date = LocalDate.of(2023, 1, 10),
      date_from = null,
      date_to = null,
      move_type = PRISON_REMAND,
      reference = "FUW7859W",
      status = "completed",
      time_due = null,
      created_at = "2023-01-10T17:16:19+00:00",
      updated_at = "2023-01-10T17:16:20+00:00",
      from_location = Location(
        id = "11f16600-d1dc-4565-977b-43fcc5548c20",
        title = "Shrewsbury County Court",
        location_type = "court",
        nomis_agency_id = "SHRWCT",
      ),
      to_location = Location(
        id = "2716a1b1-fd8e-4b24-992e-2960da4cd70c",
        title = "LINCOLN (HMP)",
        location_type = "prison",
        nomis_agency_id = "LII",
      ),
      profile = Profile(
        id = "e4cd2fdc-57f5-4c6c-b639-9b7091f43fd1",
        person = People(
          id = "89c4437f-9851-48cd-8dce-57b42f677506",
          first_names = "Genoveva",
          last_name = "Schimmel",
          date_of_birth = LocalDate.of(1988, 7, 8),
          prison_number = null,
          criminal_records_office = null,
          police_national_computer = "01/111111A",
          Gender.TRANS,
        ),
        person_escort_record = Model.PersonEscortRecords(
          id = "1f151710-2590-4964-80ce-b2d649bd7917",
          responses = arrayOf(
            Model.FrameworkResponses(
              id = "3257632c-844c-4fc0-84ed-56dee8f29961",
              responded = "true",
              value = "Did a murder",
              value_type = "string",
              question = Model.FrameworkQuestions(
                id = "9c68b6e4-509b-442d-a5ab-f09a03ced5cc",
                section = "offence-information",
                key = "current-offences",
              ),
            ),
            Model.FrameworkResponses(
              id = "7d8ab397-ab86-47e4-a4f3-c96c33a565d2",
              responded = "false",
              value = null,
              value_type = "string",
              question = Model.FrameworkQuestions(
                id = "c7186192-539f-453c-a433-4fcdea50d427",
                section = "offence-information",
                key = "current-or-previous-sex-offender",
              ),
            ),
            Model.FrameworkResponses(
              id = "22cccdd1-9640-4cbf-9700-7164f878dbe9",
              responded = "false",
              value = null,
              value_type = "string",
              question = Model.FrameworkQuestions(
                id = "2cb56004-5b6a-401f-9220-e2d30963d217",
                section = "offence-information",
                key = "terrorism-offences",
              ),
            ),
            Model.FrameworkResponses(
              id = "9f14d24c-3da6-41c8-8d86-67a96a1463d4",
              responded = "false",
              value = null,
              value_type = "string",
              question = Model.FrameworkQuestions(
                id = "1036677b-293d-48c3-b6b0-c8eb5d119449",
                section = "offence-information",
                key = "hostage-taker",
              ),
            ),
          ),
        ),
      ),
    )
  }
}
