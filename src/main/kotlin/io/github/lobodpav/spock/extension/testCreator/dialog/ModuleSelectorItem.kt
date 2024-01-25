package io.github.lobodpav.spock.extension.testCreator.dialog

import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile

data class ModuleSelectorItem(
    val displayString: String,
    val module: Module,
    val groovyTestSourceRoot: VirtualFile,
) {

    override fun toString(): String = displayString
}
