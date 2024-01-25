package io.github.lobodpav.spock.extension.testCreator.action

import com.intellij.psi.PsiElement
import io.github.lobodpav.spock.logging.Logger
import org.jetbrains.kotlin.fileClasses.javaFileFacadeFqName
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * The action invoked once the user hits the menu item in the `Go To Test` dialog
 */
class SpockForKotlinCreateTestAction : SpockCreateTestAction() {

    private companion object : Logger()

    override fun getContainingClassOrNull(psiElement: PsiElement): SourceClassInfo? {
        // Try to find a parent class first to get a fully-qualified name
        psiElement.parents
            .filterIsInstance(KtClassOrObject::class.java)
            .firstOrNull()
            ?.let {
                val containingFile = it.containingFile as? KtFile
                return SourceClassInfo(
                    qualifiedPackageName = containingFile?.packageFqName?.asString() ?: "",
                    simpleClassName = it.name ?: "",
                    qualifiedClassName = it.fqName?.asString() ?: "",
                )
            }

        // If no parent class is found, let's derive the Specification name from the Kotlin file name
        psiElement.parents
            .filterIsInstance(KtFile::class.java)
            .firstOrNull()
            ?.let {
                val containingFile = it.containingKtFile
                return SourceClassInfo(
                    qualifiedPackageName = containingFile.packageFqName.asString(),
                    simpleClassName = it.javaFileFacadeFqName.shortName().asString(),
                    qualifiedClassName = it.javaFileFacadeFqName.asString(),
                )
            }

        log.warn("Did not find any Kotlin classes, objects or files for the '$psiElement'")
        return null
    }
}

