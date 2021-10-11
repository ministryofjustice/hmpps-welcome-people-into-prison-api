package uk.gov.justice.digital.hmpps.welcometoprison.model.prison

import org.apache.commons.lang3.text.WordUtils

object Name {
  fun properCase(name: String) = WordUtils.capitalizeFully(name, '-', '\'')
}
