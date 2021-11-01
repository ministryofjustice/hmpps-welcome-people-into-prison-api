package uk.gov.justice.digital.hmpps.welcometoprison.resources

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.welcometoprison.config.ErrorResponse
import uk.gov.justice.digital.hmpps.welcometoprison.model.arrival.Arrival

@RestController
@Validated
@RequestMapping(name = "Statuses", produces = [MediaType.APPLICATION_JSON_VALUE])
class ImprisonmentReasonsResource {

  @PreAuthorize("hasRole('ROLE_VIEW_ARRIVALS')")
  @Operation(
    summary = "Produces status and reason data",
    description = "Produces imprisonment statuses and movement reasons data, requires ROLE_VIEW_ARRIVALS role",
    security = [SecurityRequirement(name = "ROLE_VIEW_ARRIVALS", scopes = ["read"])],
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "JSON object of imprisonment statuses and movement reasons",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Arrival::class))
          )
        ]
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "403",
        description = "Incorrect permissions to retrieve",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
      ),
      ApiResponse(
        responseCode = "500",
        description = "Unexpected error",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class)
          )
        ]
      ),
    ]
  )
  @GetMapping(path = ["/imprisonment-statuses"], produces = [MediaType.APPLICATION_JSON_VALUE])
  fun getStatuses(): List<ImprisonmentStatus> = imprisonmentStatuses
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "The reason for a movement into prison")
data class MovementReason(
  @Schema(description = "Reason for movement, (if required)", example = "Intermittent custodial sentence")
  val description: String? = null,

  @Schema(description = "Associated Nomis code", example = "INTER")
  val movementReasonCode: String,
)

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "The imprisonment status")
data class ImprisonmentStatus(
  @Schema(description = "The imprisonment status", example = "Determinate sentence")
  val description: String,

  @Schema(description = "Associated Nomis code", example = "SENT")
  val imprisonmentStatusCode: String,

  @Schema(description = "Title for Movement reasons page, (if required)", example = "What is the type of determinate sentence?")
  val secondLevelTitle: String? = null,

  @Schema(description = "Movement reasons data", example = "Intermittent custodial sentence, INTER ")
  val movementReasons: List<MovementReason>,
)

val imprisonmentStatuses = listOf<ImprisonmentStatus>(
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
  ImprisonmentStatus(
    description = "Recall from licence or temporary release",
    imprisonmentStatusCode = "LR_ORA",
    secondLevelTitle = "Where is the prisoner being recalled from?",
    movementReasons = listOf(
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
  ),
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
