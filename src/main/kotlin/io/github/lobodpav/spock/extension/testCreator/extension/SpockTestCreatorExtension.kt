package io.github.lobodpav.spock.extension.testCreator.extension

import com.intellij.navigation.ItemPresentation
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.testIntegration.TestCreator
import com.intellij.util.IncorrectOperationException
import io.github.lobodpav.spock.extension.testCreator.action.SpockCreateTestAction
import io.github.lobodpav.spock.extension.testCreator.visualCaretPosition
import io.github.lobodpav.spock.icon.SpockIcon
import io.github.lobodpav.spock.logging.Logger
import javax.swing.Icon

/**
 * Parent of the extensions displayed in the `Go To Test` dialog
 */
abstract class SpockTestCreatorExtension<Action : SpockCreateTestAction> : TestCreator, ItemPresentation {

    private companion object : Logger()

    override fun getPresentableText(): String = "Create New Specification"

    override fun getIcon(unused: Boolean): Icon = SpockIcon.specification

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val element = findElement(file, editor.caretModel.offset) ?: run {
            log.info("The '${getPresentableText()}' action is not available at ${file.name}:${editor.visualCaretPosition}")
            return false
        }

        return actionAvailable(element)
    }

    override fun createTest(project: Project, editor: Editor, file: PsiFile) {
        try {
            val currentElement = findElement(file, editor.caretModel.offset) ?: run {
                log.warn("Did not find PSI element at ${file.name}:${editor.visualCaretPosition}")
                return
            }

            if (actionAvailable(currentElement)) {
                action.invoke(project, editor, currentElement)
            }
        } catch (e: IncorrectOperationException) {
            log.warn("Failed to create Spock Specification", e)
        }
    }

    protected abstract fun actionAvailable(psiElement: PsiElement): Boolean
    protected abstract val action: Action

    private fun findElement(file: PsiFile, caretOffset: Int): PsiElement? =
        file.findElementAt(caretOffset)
            .takeUnless { caretOffset == file.textLength }
            ?: file.findElementAt(caretOffset - 1)
}
