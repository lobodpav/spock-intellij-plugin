package io.github.lobodpav.spock.validator

import spock.lang.Specification

class FullyQualifiedNameValidatorSpec extends Specification {

    def validator = new FullyQualifiedNameValidator()

    def "Gets an error text"() {
        expect:
        validator.validate(specificationName) == expectedErrorText

        and:
        validator.getErrorText(specificationName) == expectedErrorText

        and:
        validator.canClose(specificationName) == expectedCanClose

        where:
        specificationName || expectedErrorText                   | expectedCanClose
        ""                || "Blank qualified name"              | false
        "  "              || "Blank qualified name"              | false
        "12"              || "Not a valid Groovy qualified name" | false
        "class"           || "Not a valid Groovy qualified name" | false
        "Foo"             || null                                | true
    }
}
