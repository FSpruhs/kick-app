package com.spruhs.kick_app.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.junit.jupiter.api.Test

class HexagonalArchitectureTests {
    private val basePackage = "com.spruhs.kick_app"
    private val importedClasses = ClassFileImporter().importPackages(basePackage)

    @Test
    fun `domain should not depend on application or adapter`() {
        noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..application..", "..adapter..")
            .check(importedClasses)
    }

    @Test
    fun `application should not depend on adapter`() {
        noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..adapter..")
            .check(importedClasses)
    }
}
