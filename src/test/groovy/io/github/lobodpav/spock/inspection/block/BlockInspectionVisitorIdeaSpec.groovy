package io.github.lobodpav.spock.inspection.block

import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.SourceRoot
import io.github.lobodpav.spock.test.idea.TestModule
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static com.intellij.lang.annotation.HighlightSeverity.ERROR

class BlockInspectionVisitorIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    def setup() {
        idea.enableInspections(BlockInspection)
    }

    def "Correct block order does not highlight any error"() {
        given:
        def featureMethodBody = """
            def test() {
                ${validLabelSequence.collect { "$it: true;" }.join(" ")}
            }
        """

        and:
        idea.loadSpecWithBody(featureMethodBody)

        when:
        def blockHighlightingInfo = idea.runHighlighting()

        then:
        blockHighlightingInfo.size() == 0

        where:
        validLabelSequence << [
            ["given", "and", "and"],
            ["when", "and", "and", "then", "and", "then", "then"],
            ["expect", "and"],
            ["expect"],
            ["when", "then"],
            ["expect", "and"],
            ["expect", "and", "when", "then", "expect"],
            ["cleanup", "and", "and"],
            ["where", "and", "and"],
        ]
    }

    def "Provides an error message for the unexpected current block"() {
        given:
        def featureMethodBody = """
            def test() {
                ${previousLabels.collect { "$it: true;" }.join(" ")}
                $misplacedLabel: true              
                ${nextLabels.collect { "$it: true;" }.join(" ")}
            }
        """

        and:
        idea.loadSpecWithBody(featureMethodBody)

        when:
        def blockHighlightingInfo = idea.runHighlighting()

        then:
        blockHighlightingInfo.size() == 1
        verifyAll(blockHighlightingInfo[0]) {
            severity == ERROR
            description == expectedMessage
        }

        where:
        previousLabels                  | misplacedLabel | nextLabels       || expectedMessage
        // Invalid block names
        []                              | "foo"          | ["expect"]       || "Valid block names are 'given', 'setup', 'expect', 'when', 'then', 'cleanup', 'where', 'and'"
        ["expect"]                      | "foo"          | []               || "Valid block names are 'given', 'setup', 'expect', 'when', 'then', 'cleanup', 'where', 'and'"
        ["when"]                        | "bar"          | ["then"]         || "Valid block names are 'given', 'setup', 'expect', 'when', 'then', 'cleanup', 'where', 'and'"
        ["given"]                       | "baz"          | ["where"]        || "Valid block names are 'given', 'setup', 'expect', 'when', 'then', 'cleanup', 'where', 'and'"

        // Unexpected block after the previous one
        ["expect"]                      | "then"         | []               || "Expected one of 'when', 'cleanup', 'where', 'and', or an end of the feature method"
        ["expect", "and"]               | "then"         | []               || "Expected one of 'when', 'cleanup', 'where', 'and', or an end of the feature method"
        ["where", "and", "and"]         | "when"         | ["and"]          || "Expected 'and', or an end of the feature method"
        ["when", "and", "then", "when"] | "expect"       | ["where", "and"] || "Expected one of 'then', 'and'"

        // AND cannot be the first block
        []                              | "and"          | ["expect"]       || "Expected one of 'given', 'setup', 'expect', 'when', 'cleanup', 'where'"

        // WHEN cannot be the last block in the feature method
        ["given"]                       | "when"         | []               || "Must be followed by one of 'then', 'and'"

        // EXPECT can't follow another EXPECT
        ["expect"]                      | "expect"       | []               || "Expected one of 'when', 'cleanup', 'where', 'and', or an end of the feature method"
        ["expect", "and"]               | "expect"       | []               || "Expected one of 'when', 'cleanup', 'where', 'and', or an end of the feature method"
    }

    def "Inspections skip non-feature methods `when` block when `then` is missing"() {
        given:
        idea.loadSpecWithBody """
            def "A feature method"() {
                when:
                true
            }

            def "A non-feature method"() {
                foo:
                def value = "Text"
            }
        """

        when:
        def blockHighlightingInfo = idea.runHighlighting()

        then: "Only the feature method has got errors highlighted"
        blockHighlightingInfo.size() == 1
        verifyAll(blockHighlightingInfo[0]) {
            severity == ERROR
            description == "Must be followed by one of 'then', 'and'"
        }
    }

    def "Regular Groovy code does not get Spock errors highlighted"() {
        given:
        idea.loadFileContent("foo.bar.Test", """
            class Test {
                def test() {
                    when:
                    true
                }
            }
        """, TestModule.ROOT, SourceRoot.GROOVY_TEST)

        when:
        def blockHighlightingInfo = idea.runHighlighting()

        then:
        blockHighlightingInfo.empty
    }
}
