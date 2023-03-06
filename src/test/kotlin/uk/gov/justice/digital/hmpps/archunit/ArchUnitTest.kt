package uk.gov.justice.digital.hmpps.archunit

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes

@AnalyzeClasses(packages = ["uk.gov.justice.digital.hmpps"])
class ArchUnitTest {

  @ArchTest
  fun anyClassMustBeInBodyscanOrWelcometoprisonOrArchunit(importedClasses: JavaClasses) {
    val rule = classes().that().resideInAPackage("..uk.gov.justice.digital.hmpps..")
      .should().resideInAnyPackage(
        "..uk.gov.justice.digital.hmpps.welcometoprison..",
        "..uk.gov.justice.digital.hmpps.bodyscan..",
        "..uk.gov.justice.digital.hmpps.archunit..",
        "..uk.gov.justice.digital.hmpps.config..",
      )

    rule.orShould().haveSimpleName("App")
      .orShould().haveSimpleName("AppKt")
      .check(importedClasses)
  }

  @ArchTest
  fun anyConfigClassCanNotUseWpipOrBodyscanClasses(importedClasses: JavaClasses) {
    val rule = ArchRuleDefinition.noClasses().that().resideInAPackage("..uk.gov.justice.digital.hmpps.config..")
      .should().dependOnClassesThat().resideInAnyPackage(
        "..uk.gov.justice.digital.hmpps.bodyscan..",
        "..uk.gov.justice.digital.hmpps.welcometoprison..",
      )

    rule.check(importedClasses)
  }
}
