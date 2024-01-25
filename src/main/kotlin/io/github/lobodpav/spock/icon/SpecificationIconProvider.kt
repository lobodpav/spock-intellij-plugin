package io.github.lobodpav.spock.icon

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.groovy.ext.spock.isSpockSpecification
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition
import javax.swing.Icon

// TODO Find out why Spec icon is not refreshed when shown in the editor tabs after indexes are refreshed (Groovy icon remains).
//      Similar thing happens when a file has a syntax error before class declaration.
//      Would implementing a `FileIconProvider` help? Have a look at `GroovyFileIconProvider`.
class SpecificationIconProvider : IconProvider() {
    override fun getIcon(psiElement: PsiElement, flags: Int): Icon? =
        if (psiElement is GrClassDefinition && psiElement.isSpockSpecification()) SpockIcon.specification else null
}
