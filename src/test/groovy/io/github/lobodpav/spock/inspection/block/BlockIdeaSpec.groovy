package io.github.lobodpav.spock.inspection.block

import io.github.lobodpav.spock.inspection.block.BlockInspection
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static com.intellij.lang.annotation.HighlightSeverity.ERROR

class BlockIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    def setup() {
        idea.enableInspections(BlockInspection)
    }

    def "Highlights `when` block when `then` is missing"() {
        given:
        idea.loadSpecWithBody """
            def test() {
                when:
                true
            }
        """

        when:
        def blockHighlightingInfo = idea.runHighlighting()

        then:
        blockHighlightingInfo.size() == 1
        verifyAll(blockHighlightingInfo[0]) {
            severity == ERROR
            description == "Must be followed by one of 'then', 'and'"
        }
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
        idea.loadGroovyFileContent """
            class Test {
                def test() {
                    when:
                    true
                }
            }
        """

        when:
        def blockHighlightingInfo = idea.runHighlighting()

        then:
        blockHighlightingInfo.empty
    }
}
