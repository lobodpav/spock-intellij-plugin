package io.github.lobodpav.spock.extension.fileType

import com.intellij.openapi.fileTypes.LanguageFileType
import io.github.lobodpav.spock.icon.SpockIcon
import org.jetbrains.plugins.groovy.GroovyLanguage
import javax.swing.Icon

/**
 * Used solely for Spock templates to have a Spock icon in `Settings -> Editor -> File and Code templates`.
 */
class SpecificationFileType : LanguageFileType(GroovyLanguage) {
    override fun getName(): String = "SpockSpecification"

    override fun getDescription(): String = "Spock specification"

    override fun getDefaultExtension(): String = "spock"

    override fun getIcon(): Icon = SpockIcon.specification
}
