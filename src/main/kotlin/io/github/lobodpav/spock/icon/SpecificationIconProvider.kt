package io.github.lobodpav.spock.icon

import com.intellij.ide.IconProvider
import com.intellij.psi.PsiElement
import org.jetbrains.plugins.groovy.ext.spock.isSpockSpecification
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrClassDefinition
import javax.swing.Icon

class SpecificationIconProvider : IconProvider() {
    override fun getIcon(psiElement: PsiElement, flags: Int): Icon? =
        if (psiElement is GrClassDefinition && psiElement.isSpockSpecification()) SpockIcon.specification else null
}
