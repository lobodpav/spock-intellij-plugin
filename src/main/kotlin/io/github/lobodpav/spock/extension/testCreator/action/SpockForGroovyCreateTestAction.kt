package io.github.lobodpav.spock.extension.testCreator.action

import com.intellij.psi.PsiElement
import com.intellij.testIntegration.TestIntegrationUtils
import io.github.lobodpav.spock.logging.Logger
import org.jetbrains.plugins.groovy.lang.psi.GroovyFileBase

/**
 * The action invoked once the user hits the menu item in the `Go To Test` dialog
 */
class SpockForGroovyCreateTestAction : SpockCreateTestAction() {

    private companion object : Logger()

    override fun getContainingClassOrNull(psiElement: PsiElement): SourceClassInfo? =
        TestIntegrationUtils.findOuterClass(psiElement)
            ?.let {
                val containingFile = it.containingFile as? GroovyFileBase

                SourceClassInfo(
                    qualifiedPackageName = containingFile?.packageName ?: "",
                    simpleClassName = it.name ?: "",
                    qualifiedClassName = it.qualifiedName ?: "",
                )
            }
            ?: run {
                log.warn("Did not find any Groovy class for the '$psiElement'")
                null
            }
}
