package io.github.lobodpav.spock.extension.testCreator.extension

import com.intellij.psi.PsiElement
import io.github.lobodpav.spock.extension.testCreator.action.SpockCreateTestAction
import io.github.lobodpav.spock.extension.testCreator.action.SpockForKotlinCreateTestAction
import io.github.lobodpav.spock.ifFalse
import io.github.lobodpav.spock.logging.Logger
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedDeclaration
import org.jetbrains.kotlin.psi.psiUtil.parents

private val log = Logger.getInstance()

/**
 * The menu item displayed in the `Go To Test` dialog
 */
class SpockForKotlinTestCreatorExtension : SpockTestCreatorExtension<SpockForKotlinCreateTestAction>() {

    override fun actionAvailable(psiElement: PsiElement): Boolean =
        (psiElement.parents.firstOrNull { it is KtClassOrObject || it is KtNamedDeclaration && it.parent is KtFile } != null).ifFalse {
            log.info("The '${SpockCreateTestAction::class.simpleName}' is not available in this context")
        }

    override val action: SpockForKotlinCreateTestAction
        get() = SpockForKotlinCreateTestAction()
}
