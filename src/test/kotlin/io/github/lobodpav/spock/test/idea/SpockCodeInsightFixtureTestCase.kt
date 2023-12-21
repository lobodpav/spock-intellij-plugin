package io.github.lobodpav.spock.test.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
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
        private val projectDescriptorWithDependencies = object : DefaultLightProjectDescriptor() {
            /**
             * Makes sure that the `temp:///src` directory is not marked as a source root.
             *
             * This is necessary for the project to have two different source roots for specs to test actions
             * (i.e. `src/main/` and `src/test/`).
             */
            override fun markDirForSourcesAsSourceRoot(): Boolean = false

            override fun setUpProject(project: Project, handler: SetupHandler) {
                super.setUpProject(project, handler)

                project.modules.forEach {
                    createSourceRoot(it, "src/main/java")
                    createSourceRoot(it, "src/main/kotlin")
                    createSourceRoot(it, "src/main/groovy")
                    createSourceRoot(it, "src/test/java")
                    createSourceRoot(it, "src/test/kotlin")
                    createSourceRoot(it, "src/test/groovy")
                }
            }
        }
            // Adds Spock dependency to avoid ERROR inspections about missing spock libraries (e.g. `Specification`)
            .withRepositoryLibrary("org.spockframework:spock-core:2.3-groovy-4.0")
    }

    val fixture: CodeInsightTestFixture
        get() = super.myFixture

    override fun getProjectDescriptor(): LightProjectDescriptor = projectDescriptorWithDependencies

    fun setup() {
        super.setUp()
    }

    fun cleanup() {
        super.tearDown()
    }
}
