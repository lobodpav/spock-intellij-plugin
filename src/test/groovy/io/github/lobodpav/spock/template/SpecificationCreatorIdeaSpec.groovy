package io.github.lobodpav.spock.template

import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.WithIdea
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import spock.lang.Specification

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

        and: "The custom Specification parent is imported"
        groovyFile.read { imports.allNamedImports*.fullyQualifiedName } == ["spock.lang.Specification"]

        and: "There is a single extended Specification class"
        groovyFile.read { classes*.extendsList*.referencedTypes*.canonicalText.flatten() } == ["spock.lang.Specification"]
    }
}
