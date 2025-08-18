package com.spruhs.kick_app

import org.junit.jupiter.api.Test
import org.springframework.modulith.core.ApplicationModules
import org.springframework.modulith.docs.Documenter
import org.springframework.modulith.test.TestApplicationModules


class SpringModulithApplicationTest {

    @Test
    fun verifiesModularStructure() {
        ApplicationModules.of(KickAppApplication::class.java)
            .verify()
    }

    @Test
    fun createApplicationModuleModel() {
        val modules = TestApplicationModules.of(KickAppApplication::class.java)
        modules.forEach { module -> println(module) }
    }

    @Test
    fun createModuleDocumentation() {
        val modules = TestApplicationModules.of(KickAppApplication::class.java)
        Documenter(modules)
            .writeDocumentation()
            .writeIndividualModulesAsPlantUml()
    }
}