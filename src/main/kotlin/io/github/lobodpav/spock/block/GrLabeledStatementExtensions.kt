package io.github.lobodpav.spock.block

import com.intellij.psi.PsiElement
import com.intellij.psi.util.siblings
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrLabeledStatement

/** Converts the Groovy label into a [Block] enum  */
val GrLabeledStatement.spockBlock: Block?
    get() = Block.valueOfOrNull(this.name)

/** Finds out previous block that appeared before the current one. Any [Block.AND] blocks in between are filtered out. */
val GrLabeledStatement.previousBlock: Block?
    get() = siblings(forward = false, withSelf = false).findFirstBlockOrNull()

/** Finds out next block after the current one. Any [Block.AND] blocks in between are filtered out. */
val GrLabeledStatement.nextBlock: Block?
    get() = siblings(withSelf = false).findFirstBlockOrNull()

private fun Sequence<PsiElement>.findFirstBlockOrNull(): Block? =
    filterIsInstance<GrLabeledStatement>()
        .mapNotNull { it.spockBlock }
        .filter { it != Block.AND } // AND blocks are treated as whitespace (AND can follow any block)
        .firstOrNull()
