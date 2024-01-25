package io.github.lobodpav.spock.extension.testCreator.extension

import com.intellij.psi.PsiElement
import com.intellij.testIntegration.createTest.CreateTestAction
import io.github.lobodpav.spock.extension.testCreator.action.SpockCreateTestAction
import io.github.lobodpav.spock.extension.testCreator.action.SpockForJavaCreateTestAction
import io.github.lobodpav.spock.ifFalse
import io.github.lobodpav.spock.logging.Logger

private val log = Logger.getInstance()

/**
 * The menu item displayed in the `Go To Test` dialog
 */
class SpockForJavaTestCreatorExtension : SpockTestCreatorExtension<SpockForJavaCreateTestAction>() {

    /** Does not check if Spock is on classpath. If it's not, the generated Specification cannot be run anyway. */
    override fun actionAvailable(psiElement: PsiElement): Boolean =
        CreateTestAction.isAvailableForElement(psiElement).ifFalse {
            log.info("The '${SpockCreateTestAction::class.simpleName}' is not available in this context")
        }

    override val action: SpockForJavaCreateTestAction
        get() = SpockForJavaCreateTestAction()
}
