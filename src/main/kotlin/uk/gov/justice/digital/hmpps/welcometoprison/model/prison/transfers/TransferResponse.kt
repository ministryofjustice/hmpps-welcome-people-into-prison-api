package uk.gov.justice.digital.hmpps.welcometoprison.model.prison.transfers

data class TransferResponse(
  val prisonNumber: String,
  val location: String?
)
