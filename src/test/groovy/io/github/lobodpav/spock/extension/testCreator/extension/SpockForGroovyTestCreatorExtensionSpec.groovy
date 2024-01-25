package io.github.lobodpav.spock.extension.testCreator.extension

import io.github.lobodpav.spock.extension.testCreator.action.SpockForGroovyCreateTestAction
import io.github.lobodpav.spock.icon.SpockIcon
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.TestModule
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static io.github.lobodpav.spock.test.idea.SourceRoot.GROOVY_MAIN
import static io.github.lobodpav.spock.test.idea.ThreadingKt.invokeWriteAction

class SpockForGroovyTestCreatorExtensionSpec extends Specification {

    @WithIdea
    Idea idea

    def extension = new SpockForGroovyTestCreatorExtension()

    static def fileContent = """
        package foo.bar 
         
        class Test {
            def message = "Testing Groovy class"
        
            void test(String arg) {
                println(message)
            }
        }
        
        @interface Annotation {}
        """.stripIndent()

    def "The action is called for compatible elements"() {
        given: "The extension is created with mocked action (UI elements can't be tested)"
        def action = Mock(SpockForGroovyCreateTestAction)
        def extension = Spy(SpockForGroovyTestCreatorExtension) {
            it.action >> action
        }

        and: "A file is created"
        def psiFile = idea.loadFileContent("foo.bar.Test", fileContent, TestModule.ROOT, GROOVY_MAIN)

        and: "The caret is moved to an offset"
        invokeWriteAction { idea.editor.caretModel.moveToOffset(caretOffset) }

        when: "The extension availability is checked"
        def available = extension.readIt { it.isAvailable(idea.project, idea.editor, psiFile) }

        then:
        available == expectedAvailability

        when: "The test creation is called"
        extension.readIt { it.createTest(idea.project, idea.editor, psiFile) }

        then: "The action was called"
        interaction {
            def psiElement = psiFile.findElementAt(caretOffset)
            (expectedAvailability ? 1 : 0) * action.invoke(idea.project, idea.editor, psiElement)
        }

        where:
        caretOffset                                  || expectedAvailability
        0                                            || false
        fileContent.indexOf("message =")             || true
        fileContent.indexOf("Test {")                || true
        fileContent.indexOf("test(String")           || true
        fileContent.indexOf("@interface Annotation") || false
        fileContent.length() - 1                     || false
    }

    def "Provides correct action"() {
        expect:
        extension.action instanceof SpockForGroovyCreateTestAction
    }

    def "Provides correct extension name in the Go To Test dialog"() {
        expect:
        extension.presentableText == "Create New Specification"
    }

    def "Provides correct icon"() {
        expect:
        verifyAll(extension) {
            getIcon(false) == SpockIcon.INSTANCE.specification
            getIcon(true) == SpockIcon.INSTANCE.specification
        }
    }

}
