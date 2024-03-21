package io.github.lobodpav.spock.extension.testCreator

import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import org.jetbrains.plugins.groovy.ext.spock.SpockUtils
import org.jetbrains.plugins.groovy.util.LibrariesUtil

/** Detects if Spock is on classpath along with Groovy */
val Module.spockAvailable: Boolean get() = groovyOnClasspath && (ideInDumbMode || spockOnClasspath)

/** Finds out if IntelliJ is in dumb mode (i.e. indexing) */
private val Module.ideInDumbMode: Boolean get() = project.service<DumbService>().isDumb

private val Module.groovyOnClasspath: Boolean get() = LibrariesUtil.hasGroovySdk(this)

private val Module.spockOnClasspath: Boolean get() = LibrariesUtil.findJarWithClass(this, SpockUtils.SPEC_CLASS_NAME) != null
