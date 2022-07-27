package uk.gov.justice.digital.hmpps.archunit

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition

@AnalyzeClasses(packages = ["uk.gov.justice.digital.hmpps.welcometoprison"])
class WelcomeToPrisonArchUnitTest {

  @ArchTest
  fun anyWelcomeToPrisonClassCanNotBeUsedByBodyScanClass(importedClasses: JavaClasses) {
    val rule = ArchRuleDefinition.noClasses().that().resideInAPackage("..uk.gov.justice.digital.hmpps.welcometoprison..")
      .should().dependOnClassesThat().resideInAPackage("..uk.gov.justice.digital.hmpps.bodyscan..")
    rule.check(importedClasses)
  }
}
