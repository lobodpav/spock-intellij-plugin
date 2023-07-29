package io.github.lobodpav.spock.inspection.block

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import io.github.lobodpav.spock.inspection.block.BlockInspectionVisitor
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrLabeledStatement
import spock.lang.Specification

class BlockInspectionVisitorSpec extends Specification {

    def problemsHolder = Mock(ProblemsHolder)
    def blockInspectionVisitor = new BlockInspectionVisitor(problemsHolder)

    static def validLabelSequence = [
        "given", "and", "and",
        "when", "and", "and", "then", "and", "then", "then",
        "expect", "and",
        "expect",
        "when", "then",
        "expect", "and",
        "cleanup", "and", "and",
        "where", "and", "and",
    ]

    def "Does not register a problem for valid label sequence"() {
        given:
        def currentLabeledStatement = createLabeledStatements(previousLabels, currentLabel, nextLabels)

        when:
        blockInspectionVisitor.visitLabeledStatement(currentLabeledStatement)

        then: "No problem was registered"
        0 * problemsHolder._

        where: "Each label in the valid sequence is tested separately"
        currentLabel << validLabelSequence
        previousLabels << validLabelSequence.indexed().collect { index, _ ->
            validLabelSequence.take(index)
        }
        nextLabels << validLabelSequence.indexed(1).collect { index, _ ->
            validLabelSequence.subList(index, validLabelSequence.size())
        }
    }

    def "Provides an error message for the unexpected current block"() {
        given:
        def currentLabeledStatement = createLabeledStatements(previousLabels, currentLabel, nextLabels)

        when:
        blockInspectionVisitor.visitLabeledStatement(currentLabeledStatement)

        then: "The inspection if for the line with the label only (statement would underline the whole block below the label)"
        1 * problemsHolder.registerProblem(currentLabeledStatement.label, expectedMessage)

        where:
        previousLabels                  | currentLabel | nextLabels      || expectedMessage
        // Invalid block names
        []                              | "foo"        | []              || "Valid block names are 'given', 'setup', 'expect', 'when', 'then', 'cleanup', 'where', 'and'"
        ["when"]                        | "bar"        | ["then"]        || "Valid block names are 'given', 'setup', 'expect', 'when', 'then', 'cleanup', 'where', 'and'"
        ["given"]                       | "baz"        | ["where"]       || "Valid block names are 'given', 'setup', 'expect', 'when', 'then', 'cleanup', 'where', 'and'"

        // Unexpected block after the previous one
        ["expect", "and", "expect"]     | "then"       | []              || "Expected one of 'expect', 'when', 'cleanup', 'where', 'and', or an end of the feature method"
        ["where", "and", "and"]         | "when"       | ["and"]         || "Expected 'and', or an end of the feature method"
        ["when", "and", "then", "when"] | "expect"     | ["when", "and"] || "Expected one of 'then', 'and'"

        // AND cannot be the first block
        []                              | "and"        | ["expect"]      || "Expected one of 'given', 'setup', 'expect', 'when', 'then', 'cleanup', 'where'"

        // WHEN cannot be the last block in the feature method
        ["given"]                       | "when"       | []              || "Must be followed by one of 'then', 'and'"
    }

    /**
     * `GrLabeledStatement.previousBlock` and other Kotlin extension functions cannot be mocked
     * (extension functions are static JVM functions generated by Kotlin).
     *
     * Therefore, generating the whole series of linked {@link GrLabeledStatement}.
     */
    private GrLabeledStatement createLabeledStatements(List<String> previousLabelNames, String testedLabelName, List<String> nextLabelNames) {
        GrLabeledStatement testedElement = null
        GrLabeledStatement previousElement = null

        def allLabels = previousLabelNames + [testedLabelName] + nextLabelNames
        def testedLabelIndex = previousLabelNames.size()
        allLabels.eachWithIndex { labelName, index ->
            def currentElement = Mock(GrLabeledStatement) {
                name >> labelName
                label >> Mock(PsiElement)
                prevSibling >> previousElement
            }

            // There might be multiple labels with the same name. Hence the index check rather than label comparison.
            if (index == testedLabelIndex) testedElement = currentElement

            previousElement.nextSibling >> currentElement
            previousElement = currentElement

            currentElement
        }

        return testedElement
    }
}
