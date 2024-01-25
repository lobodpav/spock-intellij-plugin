package io.github.lobodpav.spock.template

import spock.lang.Specification

class CustomisableTemplateParameterSpec extends Specification {

    def "Provides parameter names"() {
        expect:
        CustomisableTemplateParameter.@Companion.parameterNames == ["SUPER_CLASS_NAME", "FQ_SUPER_CLASS_NAME"]
    }

    def "Creates a parameter map"() {
        expect:
        CustomisableTemplateParameter.@Companion.parameterMap(fqSuperClassname) == expectedParameterMap

        where:
        fqSuperClassname | expectedParameterMap
        "MySpec"         | ["SUPER_CLASS_NAME": "MySpec", "FQ_SUPER_CLASS_NAME": "MySpec"]
        "a.b.MySpec"     | ["SUPER_CLASS_NAME": "MySpec", "FQ_SUPER_CLASS_NAME": "a.b.MySpec"]
    }
}
