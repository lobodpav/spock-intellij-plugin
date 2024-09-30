package io.github.lobodpav.spock.extension.testCreator.dialog

import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.MutableCollectionComboBoxModel
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.dsl.gridLayout.UnscaledGaps
import io.github.lobodpav.spock.extension.fileType.Specification.CLASS_NAME_SUFFIX
import io.github.lobodpav.spock.extension.fileType.Specification.parent
import io.github.lobodpav.spock.extension.fileType.Specification.GROOVY_TEST_SOURCE_DIRECTORY_PATH
import io.github.lobodpav.spock.extension.testCreator.action.SourceClassInfo
import io.github.lobodpav.spock.logging.Logger
import io.github.lobodpav.spock.validator.ClassNameNameValidator
import io.github.lobodpav.spock.validator.FullyQualifiedNameValidator
import javax.swing.JComponent

class CreateSpecificationDialog(
    private val destinationModules: Map<ModuleSelectorItem, Set<SpecificationParent>>,
    moduleForTest: ModuleSelectorItem?,
    sourceClassInfo: SourceClassInfo,
) : DialogWrapper(false) {

    private companion object : Logger()

    private val classNameValidator = ClassNameNameValidator()
    private val packageValidator = FullyQualifiedNameValidator()
    private val hasGroovyTestSourceRoot: Boolean = moduleForTest != null
    private val dialogModel: CreateSpecificationDialogModel
    private val superClassModel: MutableCollectionComboBoxModel<SpecificationParent>

    init {
        val destinationModule = moduleForTest ?: destinationModules.keys.first()

        title = "Create Spock Specification"
        dialogModel = CreateSpecificationDialogModel(
            className = "${sourceClassInfo.simpleClassName}$CLASS_NAME_SUFFIX",
            destinationModule = destinationModule,
            destinationPackage = sourceClassInfo.qualifiedPackageName,
        )
        superClassModel = MutableCollectionComboBoxModel(destinationModules[destinationModule]?.toList() ?: emptyList())

        init()
    }

    /** Shows the dialog and returns the model if the `OK` button was pressed. Returns `null` if cancelled. */
    fun showAndGetModel(): CreateSpecificationDialogModel? =
        showAndGet()
            .takeIf { it }
            ?.let { dialogModel }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Class name:") {
                textField()
                    .align(Align.FILL)
                    .focused()
                    .bindText(dialogModel::className)
                    .validationOnInput { textField ->
                        classNameValidator.validate(textField.text)?.let { message -> error(message) }
                    }
            }

            row("Superclass:") {
                comboBox(superClassModel)
                    .comment("Abstract inheritors of the $parent")
                    .align(Align.FILL)
            }

            row("Destination module:") {
                val modulesComboBox = comboBox(destinationModules.keys)
                    .comment("Modules with an existing $GROOVY_TEST_SOURCE_DIRECTORY_PATH directory")
                    .resizableColumn()
                    .align(Align.FILL)
                    .bindItem(dialogModel::destinationModule.toNullableProperty())
                    .applyToComponent { toolTipText = item.module.name }
                    .onChanged { it.toolTipText = it.item.module.name }
                    .onChanged { comboBox ->
                        // Alter the list all available super classes from the selected destination module
                        val selectedSuperClass = superClassModel.selected
                        val newSuperClasses = destinationModules[comboBox.item] ?: emptyList()
                        superClassModel.update(newSuperClasses.toList())
                        superClassModel.selectedItem = newSuperClasses.find { it == selectedSuperClass } ?: newSuperClasses.firstOrNull()
                    }

                // Removes right gap of the combo box if the warning icon below is not visible
                if (hasGroovyTestSourceRoot) modulesComboBox.customize(UnscaledGaps.EMPTY) else modulesComboBox.gap(RightGap.SMALL)

                icon(AllIcons.General.Warning)
                    .applyToComponent { toolTipText = "There is no Groovy Test Source Root for the module the class is in" }
                    .visible(!hasGroovyTestSourceRoot)
            }

            row("Destination package:") {
                textField()
                    .align(Align.FILL)
                    .bindText(dialogModel::destinationPackage)
                    .validationOnInput {
                        // Blank package name is allowed and will create a specification in the default package (i.e. in the `src/test/groovy/` directory)
                        if (it.text.isBlank()) return@validationOnInput null
                        packageValidator.getErrorText(it.text)?.let { message -> error(message) }
                    }
            }
        }
    }
}

