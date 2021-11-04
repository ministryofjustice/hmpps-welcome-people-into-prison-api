package uk.gov.justice.digital.hmpps.welcometoprison.model.arrival

val RECALL_MOVEMENT_REASONS = listOf(
  MovementReason(
    description = "Breach of emergency temporary release",
    movementReasonCode = "ETRB"
  ),
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
  description = "Recall from licence or temporary release",
  imprisonmentStatusCode = "LR_ORA",
  secondLevelTitle = "Where is the prisoner being recalled from?",
  movementReasons = RECALL_MOVEMENT_REASONS
)

val IMPRISONMENT_STATUSES_WITH_REASONS = listOf(
  ImprisonmentStatus(
    description = "On remand",
    imprisonmentStatusCode = "RX",
    movementReasons = listOf(MovementReason(movementReasonCode = "R"))
  ),
  ImprisonmentStatus(
    description = "Convicted unsentenced",
    imprisonmentStatusCode = "JR",
    movementReasons = listOf(MovementReason(movementReasonCode = "V"))
  ),
  ImprisonmentStatus(
    description = "Determinate sentence",
    imprisonmentStatusCode = "SENT",
    secondLevelTitle = "What is the type of determinate sentence?",
    movementReasons = listOf(
      MovementReason(
        description = "Extended sentence for public protection",
        movementReasonCode = "26"
      ),
      MovementReason(
        description = "Imprisonment without option of a fine",
        movementReasonCode = "I"
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
    description = "Indeterminate sentence",
    imprisonmentStatusCode = "SENT",
    secondLevelTitle = "What is the type of indeterminate sentence?",
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
    description = "Transfer from another establishment",
    imprisonmentStatusCode = "SENT",
    secondLevelTitle = "Where is the prisoner being transferred from?",
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
    description = "Temporary stay enroute to another establishment",
    imprisonmentStatusCode = "SENT",
    secondLevelTitle = "Why is the prisoner staying at this establishment?",
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
    description = "Awaiting transfer to hospital",
    imprisonmentStatusCode = "S35MHA",
    movementReasons = listOf(
      MovementReason(
        movementReasonCode = "O"
      ),
    )
  ),
  ImprisonmentStatus(
    description = "Late return from licence",
    imprisonmentStatusCode = "LR_ORA",
    secondLevelTitle = "What is the type of late return?",
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
    description = "Detention under immigration powers",
    imprisonmentStatusCode = "DET",
    movementReasons = listOf(
      MovementReason(
        movementReasonCode = "E"
      ),
    )
  ),
  ImprisonmentStatus(
    description = "Detention in Youth Offender Institution",
    imprisonmentStatusCode = "YOI",
    movementReasons = listOf(
      MovementReason(
        movementReasonCode = "W"
      ),
    )
  ),
  ImprisonmentStatus(
    description = "Recapture after escape",
    imprisonmentStatusCode = "SENT03",
    movementReasons = listOf(
      MovementReason(
        movementReasonCode = "RECA"
      ),
    )
  ),
  ImprisonmentStatus(
    description = "Civil offence",
    imprisonmentStatusCode = "CIVIL",
    secondLevelTitle = "What is the civil offence?",
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
