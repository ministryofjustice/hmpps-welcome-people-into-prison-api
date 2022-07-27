package uk.gov.justice.digital.hmpps.archunit

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition

@AnalyzeClasses(packages = ["uk.gov.justice.digital.hmpps"])
class ArchUnitTest {

  @ArchTest
  fun anyClassMustBeInBodyscanOrWelcometoprisonOrArchunit(importedClasses: JavaClasses) {
    val rule = ArchRuleDefinition.classes().that().resideInAPackage("..uk.gov.justice.digital.hmpps..")
      .should().resideInAnyPackage("..uk.gov.justice.digital.hmpps.welcometoprison..", "..uk.gov.justice.digital.hmpps.bodyscan..", "..uk.gov.justice.digital.hmpps.archunit..")
    rule.check(importedClasses)
  }
}
