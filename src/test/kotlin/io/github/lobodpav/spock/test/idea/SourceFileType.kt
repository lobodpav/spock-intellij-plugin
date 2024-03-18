package io.github.lobodpav.spock.test.idea

import com.intellij.ide.highlighter.JavaFileType
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.plugins.groovy.GroovyFileType

enum class SourceFileType(val extension: String) {
    JAVA(JavaFileType.DEFAULT_EXTENSION),
    GROOVY(GroovyFileType.DEFAULT_EXTENSION),
    KOTLIN(KotlinFileType.EXTENSION),
}
