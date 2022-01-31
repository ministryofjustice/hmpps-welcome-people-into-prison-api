package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.apache.commons.text.WordUtils

object Name {
  fun properCase(name: String) = WordUtils.capitalizeFully(name, '-', '\'', ' ', ',')
}
