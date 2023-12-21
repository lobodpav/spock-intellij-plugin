package io.github.lobodpav.spock.test.idea

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.ide.IdeView
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.plugins.groovy.GroovyFileType

/**
 * Convenience methods allowing for Idea environment manipulation.
 */
class Idea(
    private val codeInsightTestFixture: CodeInsightTestFixture,
) {

    val project: Project = codeInsightTestFixture.project
    val module: Module = codeInsightTestFixture.module

    /**
     * Might be null if no file is loaded to the editor.
     * @see openFileInEditor
     */
    val editor: Editor? get() = codeInsightTestFixture.editor

    fun loadSpecWithBody(specBody: String): PsiFile =
        codeInsightTestFixture.configureByText(GroovyFileType.GROOVY_FILE_TYPE, specBody.wrapInSpec())

    /** Does not open the file in the editor. Call [openFileInEditor] when needed. */
    fun addSpecToProject(filePath: String, specBody: String): PsiFile =
        codeInsightTestFixture.addFileToProject(filePath, specBody.wrapInSpec())

    fun openFileInEditor(psiFile: PsiFile) {
        invokeAndWait { codeInsightTestFixture.openFileInEditor(psiFile.virtualFile) }
    }

    /**
     * Finds or creates a directory in the project's virtual file system.
     * @param relativePath Either an empty string for the root project directory, or a path to a directory inside the `/src/`.
     */
    fun findOrCreateVirtualDirectory(relativePath: String = ""): VirtualFile =
        codeInsightTestFixture.tempDirFixture.findOrCreateDir(relativePath)

    /**
     * Returns a directory in the project's virtual file system.
     * @param relativePath Either an empty string for the root project directory, or a path to a directory inside the `/src/`.
     */
    fun findOrCreateDirectory(relativePath: String = ""): PsiDirectory {
        val dir = codeInsightTestFixture.tempDirFixture.findOrCreateDir(relativePath)

        return runReadAction {
            codeInsightTestFixture.psiManager.findDirectory(dir)
                ?: throw IllegalStateException("Did not find a PSI directory for '$relativePath'")
        }
    }

    /**
     * Creates an [IdeView] instance with the single provided directory in the project's virtual file system.
     * The directory is created if it doesn't exist yet.
     * @param relativeDirectoryPath Either an empty string for the root project directory, or a path to a directory inside the `/src/`.
     */
    fun createIdeView(relativeDirectoryPath: String = ""): IdeView {
        val psiDirectory = findOrCreateDirectory(relativeDirectoryPath)

        return object : IdeView {
            override fun getDirectories(): Array<PsiDirectory> = arrayOf(psiDirectory)
            override fun getOrChooseDirectory(): PsiDirectory = psiDirectory
        }
    }

    fun enableInspections(vararg inspections: Class<LocalInspectionTool>) {
        codeInsightTestFixture.enableInspections(*inspections)
    }

    /** Returns highlighted warnings and errors */
    fun runHighlighting(): List<HighlightInfo> =
        codeInsightTestFixture.doHighlighting(HighlightSeverity.WARNING)

    /** Runs a write action command synchronously on the AWT event dispatching thread under Write Intent lock */
    fun write(runnable: () -> Unit) {
        ApplicationManager.getApplication().invokeAndWait {
            WriteCommandAction.runWriteCommandAction(project) {
                runnable()
            }
        }
    }

    fun testAction(action: AnAction): Presentation =
        codeInsightTestFixture.testAction(action)

    private fun invokeAndWait(runnable: () -> Unit) {
        ApplicationManager.getApplication().invokeAndWait { runnable() }
    }

    private fun <T> runReadAction(supplier: () -> T): T =
        ApplicationManager.getApplication().runReadAction<T> { supplier() }

    private fun <T> runReadActionOrNull(supplier: () -> T?): T? =
        ApplicationManager.getApplication().runReadAction<T> { supplier() }
}

private fun String.wrapInSpec(): String = """
    package test

    import spock.lang.Specification

    class Spec extends Specification {
        $this
    }
""".trimIndent()
