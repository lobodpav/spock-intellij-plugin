package io.github.lobodpav.spock.template

import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.WithIdea
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import spock.lang.Specification

import static io.github.lobodpav.spock.template.CustomisableTemplateParameter.FQ_SUPER_CLASS_NAME
import static io.github.lobodpav.spock.template.CustomisableTemplateParameter.SUPER_CLASS_NAME

class SpecificationCreatorIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    def "Creates a Specification using the Simple Template"() {
        given:
        def sourceDirectory = idea.findOrCreateDirectory("src/test/groovy/some/pkg")

        when: "A new Specification is created from the template"
        idea.writeCommandAction {
            SpecificationCreator.INSTANCE.createFromTemplate(sourceDirectory, "SpecFromSimpleTemplate", SpecificationTemplate.SIMPLE, [:])
        }

        and: "The created file is looked up"
        def groovyFile = sourceDirectory.read { findFile("SpecFromSimpleTemplate.groovy") as GroovyFile }

        then: "The Specification file exists"
        groovyFile

        and: "The package name is correct"
        groovyFile.read { packageName } == "some.pkg"
    }

    def "Creates a Specification using the Customisable Template"() {
        given:
        def sourceDirectory = idea.findOrCreateDirectory("src/test/groovy/some/pkg")

        when: "A new Specification is created from the template"
        idea.writeCommandAction {
            SpecificationCreator.INSTANCE.createFromTemplate(
                sourceDirectory, "SpecFromCustomisableTemplate", SpecificationTemplate.CUSTOMISABLE,
                [
                    (SUPER_CLASS_NAME.name())   : "MySpec",
                    (FQ_SUPER_CLASS_NAME.name()): "io.test.MySpec",
                ]
            )
        }

        and: "The created file is looked up"
        def groovyFile = sourceDirectory.read { findFile("SpecFromCustomisableTemplate.groovy") as GroovyFile }

        then: "The Specification file exists"
        groovyFile

        and: "The package name is correct"
        groovyFile.read { packageName } == "some.pkg"

        and: "The custom Specification parent is imported"
        groovyFile.read { imports.allNamedImports*.fullyQualifiedName } == ["io.test.MySpec"]

        and: "There is a single extended class"
        groovyFile.read { classes*.extendsList*.referencedTypes*.canonicalText.flatten() } == ["MySpec"]
    }

    def "Fails to create a Specification using the Customisable Template without parameters"() {
        given:
        def sourceDirectory = idea.findOrCreateDirectory("src/test/groovy/some/pkg")

        when: "A new Specification creation attempt is made"
        idea.writeCommandAction {
            SpecificationCreator.INSTANCE.createFromTemplate(sourceDirectory, "SpecFromCustomisableTemplate", SpecificationTemplate.CUSTOMISABLE, [:])
        }

        then:
        def e = thrown(IllegalStateException)
        e.message == "The 'CUSTOMISABLE' Specification template requires all of these parameters specified: [SUPER_CLASS_NAME, FQ_SUPER_CLASS_NAME]"
    }
}
