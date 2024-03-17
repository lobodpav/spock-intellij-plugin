package io.github.lobodpav.spock.action

import spock.lang.Specification

class SpecificationNameValidatorSpec extends Specification {

    SpecificationNameValidator specificationNameValidator

    def setup() {
        specificationNameValidator = new SpecificationNameValidator()
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
