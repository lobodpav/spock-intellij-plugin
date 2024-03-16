package io.github.lobodpav.spock.test.idea

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.findOrCreateDirectory
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.DefaultLightProjectDescriptor
import kotlin.io.path.Path

class SpockLightProjectDescriptor : DefaultLightProjectDescriptor() {

    private companion object {
        /** Gradle projects create `main` and `test` root modules */
        private const val ROOT_MODULE_NAME = "test"
        private const val TEMP_DIRECTORY = "temp:///"
    }

    override fun setUpProject(project: Project, handler: SetupHandler) {
        createAndSetupModule(project, handler, ROOT_MODULE_NAME, "")

        // Create additional modules to have a complex project setup allowing to test GoToTest actions
        createAndSetupModule(project, handler, "module1", "module1")
        createAndSetupModule(project, handler, "module2", "module2")
    }

    private fun createAndSetupModule(project: Project, handler: SetupHandler, moduleName: String, modulePath: String) {
        invokeWriteAction {
            val module = createModule(project, Path(FileUtil.getTempDirectory(), "$moduleName.iml")).also {
                handler.moduleCreated(it)
            }

            SourceRoot.entries.forEach { sourceRoot ->
                createSourceRoot(module, "$modulePath/${sourceRoot.path}", sourceRoot.test).let {
                    handler.sourceRootCreated(it)
                    createContentEntry(module, it)
                }
            }
        }
    }

    /**
     * The solution is inspired by the
     * [IntelliJ community post](https://intellij-support.jetbrains.com/hc/en-us/community/posts/16231590860946-Is-it-possible-to-have-multiple-source-roots-in-unit-tests-).
     */
    private fun createSourceRoot(module: Module, path: String, testRoot: Boolean): VirtualFile {
        val tempDir = VirtualFileManager.getInstance().findFileByUrl(TEMP_DIRECTORY) ?: error("Did not find the 'temp:///' directory to hold test project data")
        tempDir.refresh(false, false)

        val testRootDir = tempDir.findOrCreateDirectory(path)

        PsiTestUtil.addSourceRoot(module, testRootDir, testRoot)
        registerSourceRoot(module.project, testRootDir)

        return testRootDir
    }
}
