package io.github.lobodpav.spock.test.idea

import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ContentEntry
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import com.intellij.testFramework.fixtures.JavaCodeInsightTestFixture

class SpockLightProjectDescriptor(
    /**
     * The fixture is null until the [com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase.setUp] is called.
     * However, the [SpockLightProjectDescriptor] instance is created before the setup is called. Hence this closure wrapper.
     *
     * At the time [configureModule] is called, the test fixture is already setup, so this is a fairly safe workaround.
     */
    private val lateInitFixture: () -> JavaCodeInsightTestFixture,
) : DefaultLightProjectDescriptor() {

    private companion object {
        private val sourceRoots = listOf("groovy", "java", "kotlin")
    }

    /**
     * Makes sure that the `temp:///src` directory is NOT marked as a source root.
     *
     * Allows the project to have two different source roots for specs to test actions (i.e. `src/main/` and `src/test/`).
     */
    override fun markDirForSourcesAsSourceRoot(): Boolean = false

    override fun configureModule(module: Module, model: ModifiableRootModel, contentEntry: ContentEntry) {
        super.configureModule(module, model, contentEntry)

        sourceRoots.forEach {
            createSourceRoot(module, "main/$it", false)
            createSourceRoot(module, "test/$it", true)
        }
    }

    /**
     * The solution used from the
     * [IntelliJ community post](https://intellij-support.jetbrains.com/hc/en-us/community/posts/16231590860946-Is-it-possible-to-have-multiple-source-roots-in-unit-tests-).
     */
    private fun createSourceRoot(module: Module, path: String, testRoot: Boolean) {
        val testRootDir = lateInitFixture().tempDirFixture.findOrCreateDir(path)
        PsiTestUtil.addSourceRoot(module, testRootDir, testRoot)

        Disposer.register(lateInitFixture().testRootDisposable) {
            PsiTestUtil.removeSourceRoot(module, testRootDir)
        }
    }
}
