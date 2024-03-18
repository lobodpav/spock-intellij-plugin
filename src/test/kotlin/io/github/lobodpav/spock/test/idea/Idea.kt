package io.github.lobodpav.spock.test.idea

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.findOrCreateDirectory
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.replaceService
import io.github.lobodpav.spock.test.idea.SpockLightProjectDescriptor.Companion.TEMP_DIRECTORY

/**
 * Convenience methods allowing for Idea environment manipulation.
 */
class Idea(
    private val codeInsightTestFixture: CodeInsightTestFixture,
) {

    private val virtualTempDirectory = VirtualFileManager.getInstance().findFileByUrl(TEMP_DIRECTORY) ?: error("Failed to find the '$TEMP_DIRECTORY' directory where the test data are stored")

    val project: Project = codeInsightTestFixture.project
    val module: Module = codeInsightTestFixture.module

    /** Gets an editor if a file was loaded and opened in the Editor, for example by calling the [loadFileContent] */
    val editor: Editor get() = codeInsightTestFixture.editor

    /** Opens a file in the [editor]. Necessary if a file was created by the plugin code. */
    fun openFileInEditor(virtualFile: VirtualFile): Editor {
        codeInsightTestFixture.openFileInEditor(virtualFile)
        return editor
    }

    fun getTestSourceRoot(testModule: TestModule): VirtualFile {
        val module = ModuleManager.getInstance(project).modules.find { it.name == testModule.moduleName }
            ?: error("Did not find the '${testModule.moduleName}' module")

        return ModuleRootManager.getInstance(module).sourceRoots.find { it.path.endsWith(SourceRoot.GROOVY_TEST.path) }
            ?: error("Did not find the Groovy test source root")
    }

    /**
     * Creates a file with the specified content and loads it into the in-memory editor.
     *
     * The [fqClassName] is relative to the `temp:///${sourceRoot}` directory.
     * For example, for `foo.bar.Baz` and [SourceRoot.GROOVY_MAIN], a `temp:///src/main/groovy/foo/bar/Baz.groovy` file will be created.
     */
    fun loadFileContent(sourceRoot: SourceRoot, fqClassName: String, content: String): PsiFile =
        codeInsightTestFixture.addFileToProject(fqClassName.toFilePath(sourceRoot), content).also {
            invokeWriteAction {
                codeInsightTestFixture.openFileInEditor(it.virtualFile)
            }
        }

    /** Wraps the content in a class extending [spock.lang.Specification] and loads it into the in-memory editor */
    fun loadSpecWithBody(specBody: String): PsiFile =
        loadFileContent(SourceRoot.GROOVY_TEST, "foo.bar.Test", specBody.wrapInSpecification())

    /**
     * Returns a file in the project's virtual file system or null when not found.
     *
     * The virtual [TEMP_DIRECTORY] is reloaded if the file is not found at the first attempt.
     */
    fun findVirtualFile(relativePath: String): VirtualFile? =
        virtualTempDirectory.findFileByRelativePath(relativePath) ?: run {
            virtualTempDirectory.refresh(false, true)
            virtualTempDirectory.findFileByRelativePath(relativePath) ?: return null
        }


    /**
     * Returns a directory in the project's virtual file system or null when not found.
     */
    fun findDirectory(relativePath: String): PsiDirectory? {
        val virtualDirectory = findVirtualFile(relativePath) ?: return null
        return computeReadAction { codeInsightTestFixture.psiManager.findDirectory(virtualDirectory) }
    }

    /**
     * Returns a directory in the project's virtual file system.
     */
    fun findOrCreateDirectory(relativePath: String): PsiDirectory = computeWriteAction {
        val virtualDirectory = virtualTempDirectory.findOrCreateDirectory(relativePath)

        codeInsightTestFixture.psiManager.findDirectory(virtualDirectory)
            ?: throw IllegalStateException("Did not find a PSI directory for '${virtualDirectory.url}'")
    }

    /** Allows tests to switch to Dumb mode by providing a mock service */
    fun replaceDumbService(newDumbService: DumbService) {
        project.replaceService(DumbService::class.java, newDumbService, codeInsightTestFixture.testRootDisposable)
    }

    /** Enables defined inspections to be able to test highlighting of errors */
    fun enableInspections(vararg inspections: Class<LocalInspectionTool>) {
        codeInsightTestFixture.enableInspections(*inspections)
    }

    /** Returns highlighted warnings and errors */
    fun runHighlighting(): List<HighlightInfo> =
        codeInsightTestFixture.doHighlighting(HighlightSeverity.WARNING)

    /**
     * PSI write access is only allowed from inside a write-action,
     * that must be run on the AWT event dispatching thread under Write Intent lock.
     * Moreover, PSI changes must happen within a command or an undo-transparent action.
     *
     * Therefore, this extension wraps the closure inside a [WriteCommandAction.runWriteCommandAction] and waits for the result.
     *
     * @see io.github.lobodpav.spock.test.ThreadingJvmExtensions.read
     */
    fun writeCommandAction(runnable: () -> Unit) {
        // java.lang.RuntimeException: com.intellij.util.IncorrectOperationException:
        // Must not change PSI outside command or undo-transparent action.
        // See com.intellij.openapi.command.WriteCommandAction or com.intellij.openapi.command.CommandProcessor
        WriteCommandAction.runWriteCommandAction(project) {
            runnable()
        }
    }
}

private fun String.wrapInSpecification(): String = """
    package test

    import spock.lang.Specification

    class Spec extends Specification {
        $this
    }
""".trimIndent()

/**
 * Converts a fully qualified class name to path in the specified [SourceRoot].
 *
 * For example, for a `foo.bar.Baz` FQ class name and a [SourceRoot.KOTLIN_MAIN], the returned path is `src/main/kotlin/foo/bar/Baz.kt`.
 */
private fun String.toFilePath(sourceRoot: SourceRoot): String =
    "${sourceRoot.path}/${replace('.', '/')}.${sourceRoot.sourceFileType.extension}"

