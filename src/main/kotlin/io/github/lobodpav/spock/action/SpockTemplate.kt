package io.github.lobodpav.spock.action

/**
 * For actions that use file templates, both the action and template would appear in the `File -> New` menu.
 * To avoid such duplicities:
 * - Put templates in the `fileTemplates/internal` folder.
 * - Register an internal template in the `<extensions>` section of the `plugin.xml` via `<internalFileTemplate>` tag.
 *
 * See the [official documentation](https://plugins.jetbrains.com/docs/intellij/using-file-templates.html#custom-create-file-from-template-actions)
 * for more details.
 */
object SpockTemplate {
    const val SPECIFICATION = "Spock Specification.spock"
}
