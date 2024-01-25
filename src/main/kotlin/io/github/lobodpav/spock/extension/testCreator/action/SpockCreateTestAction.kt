package io.github.lobodpav.spock.extension.testCreator.action

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.testIntegration.createTest.CreateTestAction
import io.github.lobodpav.spock.extension.fileType.Specification
import io.github.lobodpav.spock.extension.testCreator.createSpecification
import io.github.lobodpav.spock.extension.testCreator.dialog.CreateSpecificationDialog
import io.github.lobodpav.spock.extension.testCreator.dialog.CreateSpecificationDialogOutput
import io.github.lobodpav.spock.extension.testCreator.findAllAbstractSpecificationInheritors
import io.github.lobodpav.spock.extension.testCreator.modulesForSelector
import io.github.lobodpav.spock.logging.Logger

/**
 * The parent of an action invoked once the user hits the menu item in the `Go To Test` dialog
 */
abstract class SpockCreateTestAction : CreateTestAction() {

    companion object : Logger() {
        const val CANNOT_CREATE_SPECIFICATION = "Cannot Create Specification"
    }

    override fun getText(): String = "Create new Specification"

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val sourceClassInfo = getContainingClassOrNull(element)
            ?: run {
                Messages.showErrorDialog(project, "No class was found found for the '${element.text}'", CANNOT_CREATE_SPECIFICATION)
                return
            }

        val sourceModule = ModuleUtilCore.findModuleForPsiElement(element)
            ?: run {
                Messages.showErrorDialog(project, "No module found for the '${sourceClassInfo.qualifiedClassName}'", CANNOT_CREATE_SPECIFICATION)
                return
            }

        val modulesForSelector = project.modulesForSelector
        if (modulesForSelector.isEmpty()) {
            Messages.showErrorDialog(project, "There is no module with a Groovy Test Source Root (i.e. '${Specification.GROOVY_TEST_SOURCE_DIRECTORY_PATH}' directory)", CANNOT_CREATE_SPECIFICATION)
            return
        }

        val suggestedModuleForTest = suggestModuleForTests(project, sourceModule)
        val moduleForTest = modulesForSelector.find { it.module == suggestedModuleForTest }
        val destinationModules = modulesForSelector.associateWith {
            setOf(Specification.parent) + it.module.findAllAbstractSpecificationInheritors()
        }

        CreateSpecificationDialog(destinationModules, moduleForTest, sourceClassInfo)
            .showAndGetModel()
            ?.let {
                val dialogOutput = CreateSpecificationDialogOutput(it.className, it.destinationModule.groovyTestSourceRoot, it.destinationPackage, it.superClass.fqClassName)
                project.createSpecification(dialogOutput)
            }
    }

    abstract fun getContainingClassOrNull(psiElement: PsiElement): SourceClassInfo?
}
