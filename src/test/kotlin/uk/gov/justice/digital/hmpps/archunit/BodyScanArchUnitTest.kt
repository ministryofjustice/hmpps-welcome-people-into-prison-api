package uk.gov.justice.digital.hmpps.archunit

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition

@AnalyzeClasses(packages = ["uk.gov.justice.digital.hmpps.bodyscan"])
class BodyScanArchUnitTest {

  @ArchTest
  fun anyBodyScanClassCanNotBeUsedByWelcomeToPrisonClass(importedClasses: JavaClasses) {
    val rule = ArchRuleDefinition.noClasses().that().resideInAPackage("..uk.gov.justice.digital.hmpps.bodyscan..")
      .should().dependOnClassesThat().resideInAPackage("..uk.gov.justice.digital.hmpps.welcometoprison..")
    rule.check(importedClasses)
  }
}
