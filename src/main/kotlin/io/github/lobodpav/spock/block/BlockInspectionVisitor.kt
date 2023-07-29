package io.github.lobodpav.spock.block

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import io.github.lobodpav.spock.logging.Logger
import org.jetbrains.plugins.groovy.ext.spock.SpockUtils
import org.jetbrains.plugins.groovy.ext.spock.isInsideSpecification
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrLabeledStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.members.GrMethod

class BlockInspectionVisitor(
    private val problemsHolder: ProblemsHolder,
) : GroovyElementVisitor() {

    private companion object : Logger() {
        private val validBlockNames: String = Block.entries.toSpockNames()

        private val validStartingBlockNames: String = Block.entries.asSequence()
            .filter { it.canBeFirstBlock }
            .toSpockNames()
    }

    override fun visitLabeledStatement(labeledStatement: GrLabeledStatement) {
        super.visitLabeledStatement(labeledStatement)

        if (!labeledStatement.isInsideSpockFeatureMethod()) {
            log.debug("Skipping label '${labeledStatement.name}' which is not inside a Spock feature method")
            return
        }

        log.debug("Visiting label '${labeledStatement.name}'")

        val currentBlock = labeledStatement.spockBlock ?: run {
            registerUnknownBlockProblem(labeledStatement)
            return
        }

        val previousBlock = labeledStatement.previousBlock
        if (previousBlock == null && !currentBlock.canBeFirstBlock) {
            registerCannotBeTheStartingBlockProblem(labeledStatement)
            return
        }

        // AND blocks are treated as whitespace and therefore skipped (AND can follow any other block)
        if (currentBlock == Block.AND) return

        if (previousBlock != null && currentBlock.mustNotFollow(previousBlock)) {
            registerUnexpectedBlockProblem(labeledStatement, previousBlock)
            return
        }

        val nextBlock = labeledStatement.nextBlock
        if (nextBlock == null && !currentBlock.canBeLastBlock) {
            registerMissingSuccessorBlockProblem(labeledStatement, currentBlock)
            return
        }
    }

    private fun registerUnknownBlockProblem(problematicStatement: GrLabeledStatement) {
        problemsHolder.registerProblem(problematicStatement.label, "Valid block names are $validBlockNames")
    }

    private fun registerCannotBeTheStartingBlockProblem(problematicStatement: GrLabeledStatement) {
        problemsHolder.registerProblem(problematicStatement.label, "Expected one of $validStartingBlockNames")
    }

    private fun registerUnexpectedBlockProblem(problematicStatement: GrLabeledStatement, previousBlock: Block) {
        val message = "Expected ${previousBlock.expectedBlockNames}" +
                if (previousBlock.canBeLastBlock) ", or an end of the feature method" else ""

        problemsHolder.registerProblem(problematicStatement.label, message)
    }

    private fun registerMissingSuccessorBlockProblem(problematicStatement: GrLabeledStatement, currentBlock: Block) {
        problemsHolder.registerProblem(problematicStatement.label, "Must be followed by ${currentBlock.expectedBlockNames}")
    }
}

/** Provides a comma-separated list of expected successor block names */
private val Block.expectedBlockNames: String
    get() {
        val allowedSuccessors = allowedDirectSuccessors.toSpockNames()
        val plural = allowedDirectSuccessors.size > 1

        return "${if (plural) "one of " else ""}$allowedSuccessors"
    }

/**
 *  Transforms a sequence of blocks into a list of comma-separated block names.
 *
 *  For example, a sequence of `listOf(WHEN, THEN)` will be transformed to `'when', 'then'`.
 *
 *  The operation is terminal.
 */
private fun Sequence<Block>.toSpockNames(): String = joinToString { "'${it.spockName}'" }

/**
 *  Transforms a collection of blocks into a list of comma-separated block names.
 *
 *  For example, a `listOf(WHEN, THEN)` will be transformed to `'when', 'then'`.
 */
private fun Iterable<Block>.toSpockNames(): String = this.joinToString { "'${it.spockName}'" }

/**
 * Finds the method in which the [PsiElement] is located and checks whether it's a Spock Feature method.
 * If so, climbs up the `PSI` tree to find a Spock specification.
 * If both conditions are met, we're inside a Spock Feature method.
 *
 * Calling this function every time a Spock block is checked is inefficient.
 * However, the [GroovyElementVisitor] does not guarantee order in which the elements are visited.
 * Sometimes, a class element is visited sooner then a method, and vice versa.
 * For this reason, we cannot cache problems and add them on class/method level visitor.
 *
 * Implementing [com.intellij.codeInsight.daemon.impl.HighlightInfoFilter]
 * could be utilised to remove problems for non-feature methods.
 * The filter approach would not improve efficiency, though.
 * Moreover, filters do not provide info about which visitor created a problem.
 * This makes it very difficult to find out Spock-specific highlights.
 */
private fun GroovyPsiElement.isInsideSpockFeatureMethod(): Boolean =
    PsiTreeUtil.getParentOfType(this, GrMethod::class.java)
        ?.takeIf { SpockUtils.isFeatureMethod(it) }
        ?.isInsideSpecification()
        ?: false
