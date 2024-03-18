package io.github.lobodpav.spock.validator

import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.psi.impl.PsiNameHelperImpl

class FullyQualifiedNameValidator : Validator<String>, InputValidatorEx {

    private val psiNameHelper = PsiNameHelperImpl.getInstance()

    /**
     * Gets an error text if the argument is not a valid fully-qualified Groovy class name.
     * Returns `null` if there was no validation error.
     */
    override fun validate(argument: String): String? = when {
        argument.isBlank()                       -> "Blank qualified name"
        !psiNameHelper.isQualifiedName(argument) -> "Not a valid Groovy qualified name"
        else                                     -> null
    }

    override fun getErrorText(className: String): String? = validate(className)

    override fun canClose(inputString: String): Boolean = getErrorText(inputString) == null
}
