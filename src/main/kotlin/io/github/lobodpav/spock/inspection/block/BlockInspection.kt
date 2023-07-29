package io.github.lobodpav.spock.inspection.block

import com.intellij.codeInspection.ProblemsHolder
import org.jetbrains.plugins.groovy.codeInspection.GroovyLocalInspectionTool
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor

class BlockInspection : GroovyLocalInspectionTool() {

    override fun buildGroovyVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): GroovyElementVisitor {
        return BlockInspectionVisitor(holder)
    }
}
