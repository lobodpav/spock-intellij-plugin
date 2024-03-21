package io.github.lobodpav.spock.template

/**
 * For actions that use file templates, both the action and template would appear in the `File -> New` menu.
 * To avoid such duplicities:
 * - Put templates in the `fileTemplates/internal` folder.
 * - Register an internal template in the `<extensions>` section of the `plugin.xml` via `<internalFileTemplate>` tag.
 *
 * See the [official documentation](https://plugins.jetbrains.com/docs/intellij/using-file-templates.html#custom-create-file-from-template-actions)
 * for more details.
 */
enum class SpecificationTemplate(val fileName: String) {
    SIMPLE("Spock Specification.spock");

    companion object {
        fun fromFileName(fileName: String): SpecificationTemplate =
            entries.find { it.fileName == fileName } ?: error("Did not find Specification template by the file name '$fileName'")
    }
}
