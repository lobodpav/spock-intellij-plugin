package io.github.lobodpav.spock.validator

import spock.lang.Specification

class ClassNameNameValidatorSpec extends Specification {

    def validator = new ClassNameNameValidator()

    def "Gets an error text"() {
        expect:
        validator.validate(specificationName) == expectedErrorText

        where:
        specificationName || expectedErrorText
        ""                || "Blank class name"
        "  "              || "Blank class name"
        "12"              || "Not a valid Groovy class name"
        "class"           || "Not a valid Groovy class name"
        "foo.Bar"         || "Dot in the class name"
        "Foo"             || null
    }
}
