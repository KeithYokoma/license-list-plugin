package io.github.jmatsu.spthanks

import io.github.jmatsu.spthanks.helper.MinimumProject
import io.github.jmatsu.spthanks.helper.setupProject
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class SpecialThanksPluginFunctionalTest {
    lateinit var project: MinimumProject

    @BeforeTest
    fun setup() {
        project = setupProject()
    }

    @AfterTest
    fun cleanup() {
        project.projectDir.deleteRecursively()
    }

    @Test
    fun `can run task`() {
        // TODO resolve missing AppPlugin error
    }
}
