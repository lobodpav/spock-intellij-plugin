package io.github.lobodpav.spock.validator

import com.intellij.psi.impl.PsiNameHelperImpl

class ClassNameNameValidator : Validator<String> {

    private val psiNameHelper = PsiNameHelperImpl.getInstance()

    /**
     * Gets an error text if the argument is not a valid Groovy class name.
     * Returns `null` if there was no validation error.
     */
    override fun validate(argument: String): String? = when {
        argument.isBlank()                       -> "Blank class name"
        !psiNameHelper.isQualifiedName(argument) -> "Not a valid Groovy class name"
        argument.contains(".")             -> "Dot in the class name"
        else                                     -> null
    }
}
