package io.github.lobodpav.spock.action

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiClassType
import io.github.lobodpav.spock.template.SpecificationTemplate
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.SpockCodeInsightFixtureTestCase
import io.github.lobodpav.spock.test.provider.DataContextProvider
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import spock.lang.Specification

class NewSpecificationActionIdeaSpec extends Specification {

    def newSpecificationAction = new NewSpecificationAction()

    def "The action produces a groovy file with a Specification class"() {
        given: "Manually setup fixture and idea to allow multiple tests defining groovy/spock classpath"
        def fixtureTestCase = new SpockCodeInsightFixtureTestCase().tap { setup() }
        def idea = new Idea(fixtureTestCase.fixture)

        and:
        def sourceDirectory = idea.findOrCreateDirectory("src/test/groovy/my/cool/name")

        when: "The action creates a new specification"
        idea.writeCommandAction {
            newSpecificationAction.doCreate(sourceDirectory, "SpecInSpec", SpecificationTemplate.SIMPLE.fileName)
        }

        and: "The created file is retrieved"
        def groovyFile = sourceDirectory.read { findFile("SpecInSpec.groovy") as GroovyFile }

        then: "A groovy file exists"
        groovyFile

        and: "The package name is correct"
        groovyFile.read { packageName } == "my.cool.name"

        when: "Extended classes are looked up"
        def extendedClasses = groovyFile.read { classes*.extendsList*.referencedTypes.flatten() } as List<PsiClassType>

        then: "There is a single extended Specification class"
        extendedClasses*.read { canonicalText } == ["spock.lang.Specification"]

        cleanup:
        fixtureTestCase.cleanup()
    }

    // TODO Setup the test project in a way where action.isAvailable() returns false for `src/main/groovy`, `src/main/java`, and `/src/main/kotlin`.
    //      See https://intellij-support.jetbrains.com/hc/en-us/community/posts/16231590860946-Is-it-possible-to-have-multiple-source-roots-in-unit-tests-
    def "The action is available only when Groovy and Spock are on the classpath"() {
        given: "Manually setup fixture and idea to allow multiple tests defining groovy/spock classpath"
        def fixtureTestCase = new SpockCodeInsightFixtureTestCase(groovyOnClasspath, spockOnClasspath).tap { setup() }
        def idea = new Idea(fixtureTestCase.fixture)

        and: "DumbService mock is injected to the test project"
        def dumbService = Mock(DumbService) { isDumb() >> dumbMode }
        idea.replaceDumbService(dumbService)

        and:
        def dataContext = DataContextProvider.make(idea, [sourceDirectory: sourceDirectory])

        when:
        def actionIsAvailable = newSpecificationAction.read { isAvailable(dataContext) }

        then:
        actionIsAvailable == expectedActionAvailability

        cleanup:
        fixtureTestCase.cleanup()

        where: "The action is available only in test sources with both Groovy and Spock on classpath"
        sourceDirectory              | dumbMode | groovyOnClasspath | spockOnClasspath || expectedActionAvailability
        // Sources
        "src/main/java/test"         | false    | true              | true             || false
        "src/main/java/test"         | true     | true              | true             || false
        "src/main/java/test/test1"   | false    | true              | true             || false
        "src/main/java/test/test1"   | true     | true              | true             || false

        "src/main/kotlin/test"       | false    | true              | true             || false
        "src/main/kotlin/test"       | true     | true              | true             || false
        "src/main/kotlin/test/test1" | false    | true              | true             || false
        "src/main/kotlin/test/test1" | true     | true              | true             || false

        "src/main/groovy/test"       | false    | true              | true             || false
        "src/main/groovy/test"       | true     | true              | true             || false

        // Test sources
        "src/test/java"              | false    | false             | false            || false
        "src/test/java"              | true     | false             | false            || false
        "src/test/java"              | false    | false             | true             || false
        "src/test/java"              | true     | false             | true             || false
        "src/test/java"              | false    | true              | false            || false
        "src/test/java"              | true     | true              | false            || true
        "src/test/java"              | false    | true              | true             || true
        "src/test/java"              | true     | true              | true             || true

        "src/test/java/test"         | false    | false             | false            || false
        "src/test/java/test"         | true     | false             | false            || false
        "src/test/java/test"         | false    | false             | true             || false
        "src/test/java/test"         | true     | false             | true             || false
        "src/test/java/test"         | false    | true              | false            || false
        "src/test/java/test"         | true     | true              | false            || true
        "src/test/java/test"         | false    | true              | true             || true
        "src/test/java/test"         | true     | true              | true             || true

        "src/test/java/test/test1"   | false    | false             | false            || false
        "src/test/java/test/test1"   | true     | false             | false            || false
        "src/test/java/test/test1"   | false    | false             | true             || false
        "src/test/java/test/test1"   | true     | false             | true             || false
        "src/test/java/test/test1"   | false    | true              | false            || false
        "src/test/java/test/test1"   | true     | true              | false            || true
        "src/test/java/test/test1"   | false    | true              | true             || true
        "src/test/java/test/test1"   | true     | true              | true             || true

        "src/test/kotlin"            | false    | false             | false            || false
        "src/test/kotlin"            | true     | false             | false            || false
        "src/test/kotlin"            | false    | false             | true             || false
        "src/test/kotlin"            | true     | false             | true             || false
        "src/test/kotlin"            | false    | true              | false            || false
        "src/test/kotlin"            | true     | true              | false            || true
        "src/test/kotlin"            | false    | true              | true             || true
        "src/test/kotlin"            | true     | true              | true             || true

        "src/test/kotlin/test"       | false    | false             | false            || false
        "src/test/kotlin/test"       | true     | false             | false            || false
        "src/test/kotlin/test"       | false    | false             | true             || false
        "src/test/kotlin/test"       | true     | false             | true             || false
        "src/test/kotlin/test"       | false    | true              | false            || false
        "src/test/kotlin/test"       | true     | true              | false            || true
        "src/test/kotlin/test"       | false    | true              | true             || true
        "src/test/kotlin/test"       | true     | true              | true             || true

        "src/test/kotlin/test/test1" | false    | false             | false            || false
        "src/test/kotlin/test/test1" | true     | false             | false            || false
        "src/test/kotlin/test/test1" | false    | false             | true             || false
        "src/test/kotlin/test/test1" | true     | false             | true             || false
        "src/test/kotlin/test/test1" | false    | true              | false            || false
        "src/test/kotlin/test/test1" | true     | true              | false            || true
        "src/test/kotlin/test/test1" | false    | true              | true             || true
        "src/test/kotlin/test/test1" | true     | true              | true             || true

        "src/test/groovy"            | false    | false             | false            || false
        "src/test/groovy"            | true     | false             | false            || false
        "src/test/groovy"            | false    | false             | true             || false
        "src/test/groovy"            | true     | false             | true             || false
        "src/test/groovy"            | false    | true              | false            || false
        "src/test/groovy"            | true     | true              | false            || true
        "src/test/groovy"            | false    | true              | true             || true
        "src/test/groovy"            | true     | true              | true             || true

        "src/test/groovy/test"       | false    | false             | false            || false
        "src/test/groovy/test"       | true     | false             | false            || false
        "src/test/groovy/test"       | false    | false             | true             || false
        "src/test/groovy/test"       | true     | false             | true             || false
        "src/test/groovy/test"       | false    | true              | false            || false
        "src/test/groovy/test"       | true     | true              | false            || true
        "src/test/groovy/test"       | false    | true              | true             || true
        "src/test/groovy/test"       | true     | true              | true             || true

        "src/test/groovy/test/test1" | false    | false             | false            || false
        "src/test/groovy/test/test1" | true     | false             | false            || false
        "src/test/groovy/test/test1" | false    | false             | true             || false
        "src/test/groovy/test/test1" | true     | false             | true             || false
        "src/test/groovy/test/test1" | false    | true              | false            || false
        "src/test/groovy/test/test1" | true     | true              | false            || true
        "src/test/groovy/test/test1" | false    | true              | true             || true
        "src/test/groovy/test/test1" | true     | true              | true             || true
    }
}
