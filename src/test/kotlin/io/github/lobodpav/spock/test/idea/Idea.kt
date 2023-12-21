package io.github.lobodpav.spock.test.idea

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import com.intellij.testFramework.replaceService
import org.jetbrains.plugins.groovy.GroovyFileType

/**
 * Convenience methods allowing for Idea environment manipulation.
 */
class Idea(
    private val codeInsightTestFixture: CodeInsightTestFixture,
) {

    val project: Project = codeInsightTestFixture.project
    val module: Module = codeInsightTestFixture.module

    /** Loads any content into the in-memory editor */
    fun loadGroovyFileContent(content: String): PsiFile =
        codeInsightTestFixture.configureByText(GroovyFileType.GROOVY_FILE_TYPE, content)

    /** Creates a file with the specified content and loads it into the in-memory editor */
    fun loadGroovyFileContent(filePath: String, content: String): PsiFile =
        codeInsightTestFixture.configureByText(filePath, content)

    /** Wraps the content in a class extending [spock.lang.Specification] and loads it into the in-memory editor */
    fun loadSpecWithBody(specBody: String): PsiFile =
        loadGroovyFileContent(specBody.wrapInSpec())

    /**
     * Returns a directory in the project's virtual file system.
     * @param relativePath Either an empty string for the `src/` source root directory, or a path to a directory inside the `src/`.
     */
    fun findOrCreateDirectory(relativePath: String = ""): PsiDirectory {
        val dir = codeInsightTestFixture.tempDirFixture.findOrCreateDir(relativePath)

        return runReadAction {
            codeInsightTestFixture.psiManager.findDirectory(dir)
                ?: throw IllegalStateException("Did not find a PSI directory for '$relativePath'")
        }
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

    private fun <T> runReadAction(supplier: () -> T): T =
        ApplicationManager.getApplication().runReadAction<T> { supplier() }
}

private fun String.wrapInSpec(): String = """
    package test

    import spock.lang.Specification

    class Spec extends Specification {
        $this
    }
""".trimIndent()
