package io.github.lobodpav.spock.test.idea

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiFile
import com.intellij.testFramework.fixtures.CodeInsightTestFixture
import org.jetbrains.plugins.groovy.GroovyFileType

/**
 * Convenience methods allowing for Idea environment manipulation.
 */
class Idea(
    private val codeInsightTestFixture: CodeInsightTestFixture,
) {

    fun loadSpecWithBody(specBody: String): PsiFile {
        val classContent = """
            package test

            import spock.lang.Specification

            class Spec extends Specification {
                $specBody
            }
        """.trimIndent()

        return loadGroovyFileContent(classContent)
    }

    fun loadGroovyFileContent(content: String): PsiFile {
        return codeInsightTestFixture.configureByText(GroovyFileType.GROOVY_FILE_TYPE, content)
    }

    fun enableInspections(vararg inspections: Class<LocalInspectionTool>) {
        codeInsightTestFixture.enableInspections(*inspections)
    }

    /** Returns highlighted warnings and errors */
    fun runHighlighting(): List<HighlightInfo> =
        codeInsightTestFixture.doHighlighting(HighlightSeverity.WARNING)
}
