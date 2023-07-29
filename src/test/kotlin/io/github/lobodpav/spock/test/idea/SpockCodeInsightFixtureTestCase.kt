package io.github.lobodpav.spock.test.idea

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

/**
 * Since Spock Specifications cannot extend both [spock.lang.Specification] and
 * [LightJavaCodeInsightFixtureTestCase], this wrapper allows for injection of the
 * fixture into specifications via the [WithIdea] annotation.
 */
class SpockCodeInsightFixtureTestCase : LightJavaCodeInsightFixtureTestCase() {

    private companion object {
        private val projectDescriptorWithDependencies = DefaultLightProjectDescriptor()
            // Adds Spock dependency to avoid ERROR inspections about missing spock libraries (e.g. `Specification`)
            .withRepositoryLibrary("org.spockframework:spock-core:2.3-groovy-4.0")
    }

    val fixture: CodeInsightTestFixture
        get() = myFixture

    override fun getProjectDescriptor(): LightProjectDescriptor = projectDescriptorWithDependencies

    fun setup() {
        super.setUp()
    }

    fun cleanup() {
        super.tearDown()
    }
}
