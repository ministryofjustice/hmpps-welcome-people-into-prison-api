package uk.gov.justice.digital.hmpps.welcometoprison.model.arrivals

val RECALL_MOVEMENT_REASONS = listOf(
  MovementReason(
    description = "Detention and Training Order",
    movementReasonCode = "Y"
  ),
  MovementReason(
    description = "Emergency temporary release",
    movementReasonCode = "ETRLR"
  ),
  MovementReason(
    description = "Error in emergency temporary release",
    movementReasonCode = "ETRRIE"
  ),
  MovementReason(
    description = "Foreign national removal scheme",
    movementReasonCode = "ETB"
  ),
  MovementReason(
    description = "Home Detention Curfew",
    movementReasonCode = "B"
  ),
  MovementReason(
    description = "Home leave",
    movementReasonCode = "H"
  ),
  MovementReason(
    description = "Intermittent custody",
    movementReasonCode = "24"
  ),
  MovementReason(
    description = "Licence",
    movementReasonCode = "L"
  ),
)

val RECALL_STATUS_AND_MOVEMENT_REASONS = ImprisonmentStatus(
  code = "recall",
  description = "Recalled",
  imprisonmentStatusCode = "RECEP_REC",
  secondLevelTitle = "Where is the prisoner being recalled from?",
  secondLevelValidationMessage = "Select where the person is being recalled from",
  movementReasons = RECALL_MOVEMENT_REASONS
)

val IMPRISONMENT_STATUSES_WITH_REASONS = listOf(
  ImprisonmentStatus(
    code = "on-remand",
    description = "On remand",
    imprisonmentStatusCode = "RECEP_REM",
    movementReasons = listOf(MovementReason(movementReasonCode = "N"))
  ),
  ImprisonmentStatus(
    code = "convicted-unsentenced",
    description = "Convicted - waiting to be sentenced",
    imprisonmentStatusCode = "RECEP_UNS",
    movementReasons = listOf(MovementReason(movementReasonCode = "V"))
  ),
  ImprisonmentStatus(
    code = "determinate-sentence",
    description = "Sentenced - fixed length of time",
    imprisonmentStatusCode = "RECEP_DET",
    secondLevelTitle = "What is the type of fixed sentence?",
    secondLevelValidationMessage = "Select the type of fixed-length sentence",
    movementReasons = listOf(
      MovementReason(
        description = "Imprisonment without option of a fine",
        movementReasonCode = "I"
      ),
      MovementReason(
        description = "Extended sentence for public protection",
        movementReasonCode = "26"
      ),
      MovementReason(
        description = "Intermittent custodial sentence",
        movementReasonCode = "INTER"
      ),
      MovementReason(
        description = "Partly suspended sentence",
        movementReasonCode = "P"
      ),
    )
  ),
  ImprisonmentStatus(
    code = "indeterminate-sentence",
    description = "Sentenced for life",
    imprisonmentStatusCode = "RECEP_IND",
    secondLevelTitle = "What is the type of indeterminate sentence?",
    secondLevelValidationMessage = "Select the type of indeterminate sentence",
    movementReasons = listOf(
      MovementReason(
        description = "Custody for life - aged under 18",
        movementReasonCode = "27"
      ),
      MovementReason(
        description = "Custody for life - aged at least 18 but under 21",
        movementReasonCode = "29"
      ),
      MovementReason(
        description = "Detained at Her Majesty's Pleasure under Section 53 (1) Children and Young Persons Act",
        movementReasonCode = "J"
      ),
      MovementReason(
        description = "For public protection",
        movementReasonCode = "25"
      ),
    )
  ),
  RECALL_STATUS_AND_MOVEMENT_REASONS,

  ImprisonmentStatus(
    code = "transfer",
    description = "Transfer from another establishment",
    imprisonmentStatusCode = "RECEP_TRA",
    secondLevelTitle = "Where is the prisoner being transferred from?",
    secondLevelValidationMessage = "Select the type of transfer",
    movementReasons = listOf(
      MovementReason(
        description = "Another establishment",
        movementReasonCode = "INT"
      ),
      MovementReason(
        description = "A foreign establishment",
        movementReasonCode = "T"
      ),
    )
  ),
  ImprisonmentStatus(
    code = "temporary-stay",
    description = "Temporary stay enroute to another establishment",
    imprisonmentStatusCode = "RECEP_TEM",
    secondLevelTitle = "Why is the prisoner staying at this establishment?",
    secondLevelValidationMessage = "Select why the person is staying at this establishment",
    movementReasons = listOf(
      MovementReason(
        description = "Sameday stopover enroute to another establishment",
        movementReasonCode = "Z"
      ),
      MovementReason(
        description = "Overnight stopover enroute to another establishment",
        movementReasonCode = "S"
      ),
      MovementReason(
        description = "Overnight stopover for accumulated visits",
        movementReasonCode = "Q"
      ),
    )
  ),
  ImprisonmentStatus(
    code = "awaiting-transfer-to-hospital",
    description = "Awaiting transfer to hospital",
    imprisonmentStatusCode = "RECEP_HOS",
    movementReasons = listOf(
      MovementReason(
        movementReasonCode = "O"
      ),
    )
  ),
  ImprisonmentStatus(
    code = "late-return",
    description = "Late return from Release on Temporary Licence (ROTL)",
    imprisonmentStatusCode = "RECEP_LAT",
    secondLevelTitle = "What is the type of late return?",
    secondLevelValidationMessage = "Select the type of late return",
    movementReasons = listOf(
      MovementReason(
        description = "Voluntary",
        movementReasonCode = "A"
      ),
      MovementReason(
        description = "Involuntary",
        movementReasonCode = "U"
      ),
    )
  ),
  ImprisonmentStatus(
    code = "detention-under-immigration-powers",
    description = "Detention under immigration powers",
    imprisonmentStatusCode = "RECEP_IMM",
    movementReasons = listOf(
      MovementReason(
        movementReasonCode = "E"
      ),
    )
  ),
  ImprisonmentStatus(
    code = "youth-offender",
    description = "Detention in Youth Offender Institution",
    imprisonmentStatusCode = "RECEP_YOI",
    movementReasons = listOf(
      MovementReason(
        movementReasonCode = "W"
      ),
    )
  ),
  ImprisonmentStatus(
    code = "recapture",
    description = "Recapture after escape",
    imprisonmentStatusCode = "RECEP_CAP",
    movementReasons = listOf(
      MovementReason(
        movementReasonCode = "RECA"
      ),
    )
  ),
  ImprisonmentStatus(
    code = "civil-offence",
    description = "Civil offence",
    imprisonmentStatusCode = "RECEP_CIV",
    secondLevelTitle = "What is the civil offence?",
    secondLevelValidationMessage = "Select the civil offence",
    movementReasons = listOf(
      MovementReason(
        description = "Civil committal",
        movementReasonCode = "C"
      ),
      MovementReason(
        description = "Non-payment of a fine",
        movementReasonCode = "F"
      ),
    )
  ),
)

val RECALL_MOVEMENT_REASON_CODES = RECALL_MOVEMENT_REASONS.map { it.movementReasonCode }.toSet()

val ALL_MOVEMENT_REASON_CODES =
  IMPRISONMENT_STATUSES_WITH_REASONS.flatMap { status ->
    status.movementReasons.map { reason ->
      reason.movementReasonCode
    }
  }.toSet()
