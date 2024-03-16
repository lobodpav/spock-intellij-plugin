package io.github.lobodpav.spock.test.idea

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.fileTypes.FileType
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.plugins.groovy.GroovyFileType

enum class SourceFileType(val fileType: FileType, val extension: String) {
    JAVA(JavaFileType.INSTANCE, JavaFileType.DEFAULT_EXTENSION),
    GROOVY(GroovyFileType.GROOVY_FILE_TYPE, GroovyFileType.DEFAULT_EXTENSION),
    KOTLIN(KotlinFileType.INSTANCE, KotlinFileType.EXTENSION),
}
