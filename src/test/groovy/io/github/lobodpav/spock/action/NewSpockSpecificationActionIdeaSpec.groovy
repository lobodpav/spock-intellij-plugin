package io.github.lobodpav.spock.action

import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiJavaFile
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.SpockCodeInsightFixtureTestCase
import io.github.lobodpav.spock.test.provider.DataContextProvider
import spock.lang.Specification

class NewSpockSpecificationActionIdeaSpec extends Specification {

    def newSpockSpecificationAction = new NewSpockSpecificationAction()

    def "The action produces a groovy file with a Specification class"() {
        given: "Manually setup fixture and idea to allow multiple tests defining groovy/spock classpath"
        def fixtureTestCase = new SpockCodeInsightFixtureTestCase().tap { setup() }
        def idea = new Idea(fixtureTestCase.fixture)

        and:
        def sourceDirectory = idea.findOrCreateDirectory("test/groovy/my/cool/name")

        when: "The action creates a new specification"
        idea.write {
            newSpockSpecificationAction.doCreate(sourceDirectory, "SpecInSpec", SpockTemplate.SPECIFICATION)
        }

        and: "The created file is retrieved"
        def psiJavaFile = sourceDirectory.read { findFile("SpecInSpec.groovy") as PsiJavaFile }

        then: "A groovy file exists"
        psiJavaFile

        and: "The package name is correct"
        psiJavaFile.read { packageName } == "my.cool.name"

        when: "Extended classes are looked up"
        def extendedClasses = psiJavaFile.read { classes*.extendsList*.referencedTypes.flatten() } as List<PsiClassType>

        then: "There is a single extended Specification class"
        extendedClasses*.read { canonicalText } == ["spock.lang.Specification"]

        cleanup:
        fixtureTestCase.cleanup()
    }

    // TODO Setup the test project in a way where action.isAvailable() returns false for `src/main/java` and others.
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
        def actionIsAvailable = newSpockSpecificationAction.read { isAvailable(dataContext) }

        then:
        actionIsAvailable == expectedActionAvailability

        cleanup:
        fixtureTestCase.cleanup()

        where: "The action is available only in test sources with both Groovy and Spock on classpath"
        sourceDirectory          | dumbMode | groovyOnClasspath | spockOnClasspath || expectedActionAvailability
        // Sources
        "main/java/test"         | false    | true              | true             || false
        "main/java/test"         | true     | true              | true             || false
        "main/java/test/test1"   | true     | true              | true             || false
        "main/java/test/test1"   | false    | true              | true             || false

        "main/kotlin/test"       | false    | true              | true             || false
        "main/kotlin/test"       | true     | true              | true             || false
        "main/kotlin/test/test1" | false    | true              | true             || false
        "main/kotlin/test/test1" | true     | true              | true             || false

        "main/groovy/test"       | false    | true              | true             || false
        "main/groovy/test"       | true     | true              | true             || false

        // Test sources
        "test/java"              | false    | false             | false            || false
        "test/java"              | true     | false             | false            || false
        "test/java"              | false    | false             | true             || false
        "test/java"              | true     | false             | true             || false
        "test/java"              | false    | true              | false            || false
        "test/java"              | true     | true              | false            || true
        "test/java"              | false    | true              | true             || true
        "test/java"              | true     | true              | true             || true

        "test/java/test"         | false    | false             | false            || false
        "test/java/test"         | true     | false             | false            || false
        "test/java/test"         | false    | false             | true             || false
        "test/java/test"         | true     | false             | true             || false
        "test/java/test"         | false    | true              | false            || false
        "test/java/test"         | true     | true              | false            || true
        "test/java/test"         | false    | true              | true             || true
        "test/java/test"         | true     | true              | true             || true

        "test/java/test/test1"   | false    | false             | false            || false
        "test/java/test/test1"   | true     | false             | false            || false
        "test/java/test/test1"   | false    | false             | true             || false
        "test/java/test/test1"   | true     | false             | true             || false
        "test/java/test/test1"   | false    | true              | false            || false
        "test/java/test/test1"   | true     | true              | false            || true
        "test/java/test/test1"   | false    | true              | true             || true
        "test/java/test/test1"   | true     | true              | true             || true

        "test/kotlin"            | false    | false             | false            || false
        "test/kotlin"            | true     | false             | false            || false
        "test/kotlin"            | false    | false             | true             || false
        "test/kotlin"            | true     | false             | true             || false
        "test/kotlin"            | false    | true              | false            || false
        "test/kotlin"            | true     | true              | false            || true
        "test/kotlin"            | false    | true              | true             || true
        "test/kotlin"            | true     | true              | true             || true

        "test/kotlin/test"       | false    | false             | false            || false
        "test/kotlin/test"       | true     | false             | false            || false
        "test/kotlin/test"       | false    | false             | true             || false
        "test/kotlin/test"       | true     | false             | true             || false
        "test/kotlin/test"       | false    | true              | false            || false
        "test/kotlin/test"       | true     | true              | false            || true
        "test/kotlin/test"       | false    | true              | true             || true
        "test/kotlin/test"       | true     | true              | true             || true

        "test/kotlin/test/test1" | false    | false             | false            || false
        "test/kotlin/test/test1" | true     | false             | false            || false
        "test/kotlin/test/test1" | false    | false             | true             || false
        "test/kotlin/test/test1" | true     | false             | true             || false
        "test/kotlin/test/test1" | false    | true              | false            || false
        "test/kotlin/test/test1" | true     | true              | false            || true
        "test/kotlin/test/test1" | false    | true              | true             || true
        "test/kotlin/test/test1" | true     | true              | true             || true

        "test/groovy"            | false    | false             | false            || false
        "test/groovy"            | true     | false             | false            || false
        "test/groovy"            | false    | false             | true             || false
        "test/groovy"            | true     | false             | true             || false
        "test/groovy"            | false    | true              | false            || false
        "test/groovy"            | true     | true              | false            || true
        "test/groovy"            | false    | true              | true             || true
        "test/groovy"            | true     | true              | true             || true

        "test/groovy/test"       | false    | false             | false            || false
        "test/groovy/test"       | true     | false             | false            || false
        "test/groovy/test"       | false    | false             | true             || false
        "test/groovy/test"       | true     | false             | true             || false
        "test/groovy/test"       | false    | true              | false            || false
        "test/groovy/test"       | true     | true              | false            || true
        "test/groovy/test"       | false    | true              | true             || true
        "test/groovy/test"       | true     | true              | true             || true

        "test/groovy/test/test1" | false    | false             | false            || false
        "test/groovy/test/test1" | true     | false             | false            || false
        "test/groovy/test/test1" | false    | false             | true             || false
        "test/groovy/test/test1" | true     | false             | true             || false
        "test/groovy/test/test1" | false    | true              | false            || false
        "test/groovy/test/test1" | true     | true              | false            || true
        "test/groovy/test/test1" | false    | true              | true             || true
        "test/groovy/test/test1" | true     | true              | true             || true
    }
}
