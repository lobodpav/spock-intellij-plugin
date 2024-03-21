package io.github.lobodpav.spock.template

import com.intellij.openapi.components.service
import com.intellij.psi.PsiDirectory
import com.intellij.psi.codeStyle.CodeStyleManager
import com.intellij.util.IncorrectOperationException
import org.jetbrains.plugins.groovy.GroovyBundle
import org.jetbrains.plugins.groovy.actions.GroovyTemplatesFactory
import org.jetbrains.plugins.groovy.actions.NewGroovyActionBase
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition

object SpecificationCreator {

    /**
     * Creates a new Specification from a named template in the specified directory.
     * Allows specifying [parameters] that will be replaced in the template if the map keys match.
     */
    fun createFromTemplate(psiDirectory: PsiDirectory, className: String, specificationTemplate: SpecificationTemplate, parameters: Map<String, String> = emptyMap()): GrTypeDefinition {
        val fileName = "$className${NewGroovyActionBase.GROOVY_EXTENSION}"

        val psiFileFromTemplate = GroovyTemplatesFactory.createFromTemplate(
            psiDirectory, className, fileName, specificationTemplate.fileName, true, *parameters.flatMap { listOf(it.key, it.value) }.toTypedArray()
        )

        if (psiFileFromTemplate is GroovyFile) {
            psiFileFromTemplate.project.service<CodeStyleManager>().reformat(psiFileFromTemplate)
            return psiFileFromTemplate.typeDefinitions[0]
        }

        throw IncorrectOperationException(
            GroovyBundle.message(
                "groovy.file.extension.is.not.mapped.to.groovy.file.type", psiFileFromTemplate.fileType.description,
            ),
        )
    }

}
