package io.github.lobodpav.spock.extension.testCreator

import com.intellij.openapi.editor.Editor

/** Caret position for printing in the form of `row:line` */
val Editor.visualCaretPosition: String
    get() = "${caretModel.visualPosition.column}:${caretModel.visualPosition.line}"
