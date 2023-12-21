package io.github.lobodpav.spock.action


import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

class SpecificationNameValidatorIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    SpecificationNameValidator specificationNameValidator

    def setup() {
        specificationNameValidator = new SpecificationNameValidator(idea.project)
    }

    def "Gets an error text"() {
        when:
        def errorText = specificationNameValidator.getErrorText(specificationName)

        then:
        errorText == expectedErrorText

        when:
        def canClose = specificationNameValidator.canClose(specificationName)

        then:
        canClose == expectedCanClose

        where:
        specificationName || expectedErrorText                   | expectedCanClose
        ""                || "Blank Specification name"          | false
        "  "              || "Blank Specification name"          | false
        "12"              || "Not a valid Groovy qualified name" | false
        "class"           || "Not a valid Groovy qualified name" | false
        "Foo"             || null                                | true
    }
}
