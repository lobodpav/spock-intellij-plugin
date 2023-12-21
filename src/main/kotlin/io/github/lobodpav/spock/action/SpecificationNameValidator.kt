package io.github.lobodpav.spock.action

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.psi.PsiNameHelper

class SpecificationNameValidator(project: Project) : InputValidatorEx {

    private val psiNameHelper = project.service<PsiNameHelper>()

    override fun getErrorText(inputString: String): String? =
        when {
            inputString.isBlank()                       -> "Blank Specification name"
            !psiNameHelper.isQualifiedName(inputString) -> "Not a valid Groovy qualified name"
            else                                        -> null
        }

    override fun canClose(inputString: String): Boolean = getErrorText(inputString) == null
}
