package uk.gov.justice.digital.hmpps.welcometoprison.utils

class LoadJsonHelper {
  companion object {
    fun String.loadJson(testClass: Any): String =
      testClass::class.java.getResource("$this.json")?.readText()
        ?: throw AssertionError("file $this.json not found")
  }
}
