package io.github.lobodpav.spock.extension.testCreator

import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.searches.ClassInheritorsSearch
import io.github.lobodpav.spock.extension.fileType.Specification
import io.github.lobodpav.spock.extension.testCreator.dialog.SpecificationParent
import io.github.lobodpav.spock.logging.Logger
import org.jetbrains.kotlin.idea.base.util.allScope
import org.jetbrains.plugins.groovy.ext.spock.SpockUtils
import org.jetbrains.plugins.groovy.util.LibrariesUtil

private val log = Logger.getInstance()

/** Detects if Spock is on classpath along with Groovy */
val Module.spockAvailable: Boolean get() = groovyOnClasspath && (ideInDumbMode || spockOnClasspath)

/** Finds out if IntelliJ is in dumb mode (i.e. indexing) */
private val Module.ideInDumbMode: Boolean get() = project.service<DumbService>().isDumb

private val Module.groovyOnClasspath: Boolean get() = LibrariesUtil.hasGroovySdk(this)

private val Module.spockOnClasspath: Boolean get() = LibrariesUtil.findJarWithClass(this, SpockUtils.SPEC_CLASS_NAME) != null

fun Module.findAllAbstractSpecificationInheritors(): Set<SpecificationParent> {
    val searchScope = getModuleWithDependenciesAndLibrariesScope(true)

    val specificationPsiClass =
        JavaPsiFacade.getInstance(project).findClass(Specification.parent.fqClassName, project.allScope())
            ?: run {
                log.warn("Did not find the '${Specification.parent}' class")
                return emptySet()
            }

    return ClassInheritorsSearch.search(specificationPsiClass, searchScope, true)
        .asSequence()
        .filter { it.hasModifierProperty(PsiModifier.ABSTRACT) }
        .mapNotNull { it.qualifiedName }
        .sorted()
        .map { SpecificationParent(it) }
        .toSet()
}
