package io.github.lobodpav.spock.test.idea

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.findOrCreateDirectory
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.replaceService

/**
 * Convenience methods allowing for Idea environment manipulation.
 */
class Idea(
    private val codeInsightTestFixture: CodeInsightTestFixture,
) {

    val project: Project = codeInsightTestFixture.project
    val module: Module = codeInsightTestFixture.module
    val editor: Editor get() = codeInsightTestFixture.editor

    private val virtualTempDirectory = VirtualFileManager.getInstance().findFileByUrl("temp:///") ?: error("Failed to find the 'temp:///' directory where the test data are stored")

    /**
     * Creates a file with the specified content and loads it into the in-memory editor.
     *
     * The [fqClassName] is relative to the `temp:///src/${sourceRoot}` directory.
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
     * Returns a directory in the project's virtual file system or null when not found.
     * @param relativePath Either an empty string for the `src/` root directory, or a path to a directory inside the `src/`.
     */
    fun findDirectory(relativePath: String = ""): PsiDirectory? {
        val virtualDirectory = virtualTempDirectory.findFileByRelativePath(relativePath) ?: run {
            virtualTempDirectory.refresh(false, true)
            virtualTempDirectory.findFileByRelativePath(relativePath) ?: return null
        }

        return computeReadAction { codeInsightTestFixture.psiManager.findDirectory(virtualDirectory) }
    }

    /**
     * Returns a directory in the project's virtual file system.
     * @param relativePath Either an empty string for the `src/` root directory, or a path to a directory inside the `src/`.
     */
    fun findOrCreateDirectory(relativePath: String = ""): PsiDirectory = computeWriteAction {
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
     * @see io.github.lobodpav.spock.test.PsiElementJvmExtensions.read
     */
    fun write(runnable: () -> Unit) {
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

