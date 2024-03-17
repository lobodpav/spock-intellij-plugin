package io.github.lobodpav.spock.action

import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.psi.impl.PsiNameHelperImpl

class SpecificationNameValidator : InputValidatorEx {

    private val psiNameHelper = PsiNameHelperImpl.getInstance()

    /**
     * Gets an error text if the argument is not a valid Groovy class name.
     * Returns `null` if there was no validation error.
     */
    override fun getErrorText(className: String): String? = when {
        className.isBlank()                       -> "Blank Specification name"
        !psiNameHelper.isQualifiedName(className) -> "Not a valid Groovy qualified name"
        else                                      -> null
    }

    override fun canClose(inputString: String): Boolean = getErrorText(inputString) == null
}
