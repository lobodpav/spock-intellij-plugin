package io.github.lobodpav.spock.action

import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import io.github.lobodpav.spock.icon.SpockIcon
import io.github.lobodpav.spock.logging.Logger
import io.github.lobodpav.spock.template.SpecificationCreator
import io.github.lobodpav.spock.validator.FullyQualifiedNameValidator
import org.jetbrains.jps.model.java.JavaSourceRootType
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition

class NewSpecificationAction : JavaCreateTemplateInPackageAction<GrTypeDefinition>(
    { ACTION_NAME },
    { "Creates a new $ACTION_NAME" },
    { SpockIcon.specification },
    setOf(JavaSourceRootType.TEST_SOURCE),
), DumbAware {

    private companion object : Logger() {
        private const val ACTION_NAME = "Spock Specification"
    }

    override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
        builder
            .setTitle("New $ACTION_NAME")
            .addKind("Class", SpockIcon.specification, SpecificationTemplate.SIMPLE.fileName)
            .setValidator(FullyQualifiedNameValidator())
    }

    override fun isAvailable(dataContext: DataContext): Boolean {
        val module = dataContext.getData(LangDataKeys.MODULE) ?: return false

        // Skip Spock classpath check when in Dumb mode to allow users to code Specifications during indexing
        return super.isAvailable(dataContext) && module.spockAvailable
    }

    override fun getActionName(directory: PsiDirectory, newName: String, templateName: String): String = ACTION_NAME

    /**
     * Place the cursor at the first sample method in the created Specification.
     * Defaults to the left curly brace of the new Specification class.
     */
    override fun getNavigationElement(createdElement: GrTypeDefinition): PsiElement? =
        createdElement.body?.methods?.get(0) ?: createdElement.lBrace

    public override fun doCreate(psiDirectory: PsiDirectory, className: String, templateName: String): GrTypeDefinition {
        val specificationTemplate = SpecificationTemplate.fromFileName(templateName)
        return SpecificationCreator.createFromTemplate(psiDirectory, className, specificationTemplate)
    }
}
