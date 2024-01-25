package io.github.lobodpav.spock.extension.testCreator

import com.intellij.openapi.command.executeCommand
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.modules
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiManager
import com.intellij.util.IncorrectOperationException
import io.github.lobodpav.spock.extension.fileType.Specification
import io.github.lobodpav.spock.extension.testCreator.action.SpockCreateTestAction
import io.github.lobodpav.spock.extension.testCreator.dialog.CreateSpecificationDialogOutput
import io.github.lobodpav.spock.extension.testCreator.dialog.ModuleSelectorItem
import io.github.lobodpav.spock.logging.Logger
import io.github.lobodpav.spock.template.CustomisableTemplateParameter
import io.github.lobodpav.spock.template.SpecificationCreator
import io.github.lobodpav.spock.template.SpecificationTemplate
import org.jetbrains.plugins.groovy.actions.NewGroovyActionBase

private val log = Logger.getInstance()

/**
 * Returns list of modules that have test source root matching
 * [io.github.lobodpav.spock.extension.fileType.Specification.GROOVY_TEST_SOURCE_DIRECTORY_PATH].
 *
 * To keep the names short and readable for Gradle projects, if all modules
 * - begin with project name, the project name is stripped off.
 * - end with `.test`, the suffix is stripped off.
 *
 * Having this file system structure
 * ```
 * /project-name/
 *     src/main/kotlin/
 *     src/test/groovy/
 *     src/test/kotlin/
 *     sub-project/
 *         src/main/java/
 *         src/main/kotlin/
 *         src/test/java/
 * ```
 * With Gradle, the [com.intellij.openapi.module.ModuleManager.modules] returns
 * - `project-name`
 * - `project-name.main`
 * - `project-name.test`
 * - `project-name.sub-project`
 * - `project-name.sub-project.main`
 * - `project-name.sub-project.test`
 *
 * With Maven, the [com.intellij.openapi.module.ModuleManager.modules] returns
 * - `project-name`
 * - `sub-project`
 */
val Project.modulesForSelector: Set<ModuleSelectorItem> get() {
    val rootGradleModuleName = rootGradleModuleName()
    val moduleToGroovyTestSourceRoot = modules.mapNotNull { module ->
        module.rootManager.sourceRoots.find { it.path.endsWith(Specification.GROOVY_TEST_SOURCE_DIRECTORY_PATH) }?.let {
            module to it
        }
    }.toMap()

    val allContainProjectNamePrefix = moduleToGroovyTestSourceRoot.all { it.key.name.startsWith("${rootGradleModuleName}.") }
    val allContainTestSuffix = moduleToGroovyTestSourceRoot.all { it.key.name.endsWith(GRADLE_TEST_MODULE_NAME_SUFFIX) }

    return moduleToGroovyTestSourceRoot.asSequence()
        .map { moduleToGroovySourceRoot ->
            // Make Gradle module names more readable by stripping out prefixes/suffixed that are the same for all modules
            val shortenedModuleName = moduleToGroovySourceRoot.key.name
                .let { if (allContainProjectNamePrefix) it.removePrefix("${rootGradleModuleName}.") else it }
                .let { if (allContainTestSuffix) it.removeSuffix(GRADLE_TEST_MODULE_NAME_SUFFIX) else it }
                .let { if (it == GRADLE_TEST_MODULE_NAME) "<project-root>" else it }

            ModuleSelectorItem(shortenedModuleName, moduleToGroovySourceRoot.key, moduleToGroovySourceRoot.value)
        }
        .sortedBy { it.displayString }
        .toSet()
}

fun Project.createSpecification(dialogOutput: CreateSpecificationDialogOutput) {
    executeCommand(this, "Create New Specification") {
        val groovyTestRootPsiDirectory =
            PsiManager.getInstance(this).findDirectory(dialogOutput.groovyTestSourceRoot)
                ?: run {
                    log.warn("Failed to find PsiDirectory for the selected destination module (${dialogOutput.groovyTestSourceRoot}. Was the test source root removed while the user was defining new Specification parameters in the dialog?")
                    Messages.showErrorDialog(this, "Failed to find directory for ${dialogOutput.groovyTestSourceRoot}", SpockCreateTestAction.CANNOT_CREATE_SPECIFICATION)
                    return@executeCommand
                }

        // Create directory for the destination module
        val newTestClassPsiDirectory = try {
            groovyTestRootPsiDirectory.createPackage(dialogOutput.destinationPackage)
        } catch (e: IncorrectOperationException) {
            log.warn("Failed to create PsiDirectory for the destination package. Test class directory: '$groovyTestRootPsiDirectory'. Destination package: '${dialogOutput.destinationPackage}'", e)
            Messages.showErrorDialog(this, "Failed to create directory for the test", SpockCreateTestAction.CANNOT_CREATE_SPECIFICATION)
            return@executeCommand
        }

        val foundOrCreatedSpecification =
            newTestClassPsiDirectory.findFile("${dialogOutput.className}${NewGroovyActionBase.GROOVY_EXTENSION}")
                ?: SpecificationCreator.createFromTemplate(
                    newTestClassPsiDirectory, dialogOutput.className, SpecificationTemplate.CUSTOMISABLE,
                    CustomisableTemplateParameter.parameterMap(dialogOutput.fqSuperClassName)
                ).containingFile

        // Open the created Class name in the editor
        val openFileDescriptor = OpenFileDescriptor(this, foundOrCreatedSpecification.containingFile.virtualFile)
        val newSpecificationEditor = FileEditorManager.getInstance(this).openTextEditor(openFileDescriptor, true)
            ?: run {
                log.warn("No file editor was opened after Specification creation")
                return@executeCommand
            }

        // Move the caret to the created Class name
        newSpecificationEditor.caretModel.moveToOffset(foundOrCreatedSpecification.textOffset)
    }
}

private const val GRADLE_TEST_MODULE_NAME = "test"
private const val GRADLE_TEST_MODULE_NAME_SUFFIX = ".$GRADLE_TEST_MODULE_NAME"

/**
 * Maven module names cant contain spaces because the module names are derived from Artifact ID that can't contain spaces.
 *
 * Gradle, on the other hand, can have spaces in the project name. Project name is used to generate root module name.
 * Spaces are replaced by underscores. For example, a `Foo Bar` project name will have a `Foo_Bar` root module name.
 */
private fun Project.rootGradleModuleName(): String = name.replace(' ', '_')

