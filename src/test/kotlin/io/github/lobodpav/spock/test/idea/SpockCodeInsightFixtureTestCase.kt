package io.github.lobodpav.spock.test.idea

import com.intellij.testFramework.LightProjectDescriptor
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase

/**
 * Since Spock Specifications cannot extend both [spock.lang.Specification] and
 * [LightJavaCodeInsightFixtureTestCase], this wrapper allows for injection of the
 * fixture into specifications via the [WithIdea] annotation.
 *
 * The [groovyOnClasspath] and [spockOnClasspath] arguments allow custom setup
 * for [com.intellij.openapi.actionSystem.AnAction] testing.
 */
class SpockCodeInsightFixtureTestCase(
    private val groovyOnClasspath: Boolean = true,
    private val spockOnClasspath: Boolean = true,
) : LightJavaCodeInsightFixtureTestCase() {

    override fun getProjectDescriptor(): LightProjectDescriptor = SpockLightProjectDescriptor().apply {

        // Adds Spock dependency to avoid ERROR inspections about missing spock libraries (e.g. `Specification`)
        if (groovyOnClasspath) withRepositoryLibrary("org.apache.groovy:groovy:4.0.13", false)
        if (spockOnClasspath) withRepositoryLibrary("org.spockframework:spock-core:2.3-groovy-4.0", false)
    }

    val fixture: JavaCodeInsightTestFixture
        get() = super.myFixture

    fun setup() {
        super.setUp()
    }

    fun cleanup() {
        super.tearDown()
    }
}
