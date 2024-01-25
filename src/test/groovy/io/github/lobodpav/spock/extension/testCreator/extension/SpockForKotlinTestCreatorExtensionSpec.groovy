package io.github.lobodpav.spock.extension.testCreator.extension

import io.github.lobodpav.spock.extension.testCreator.action.SpockForKotlinCreateTestAction
import io.github.lobodpav.spock.icon.SpockIcon
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.TestModule
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static io.github.lobodpav.spock.test.idea.SourceRoot.KOTLIN_MAIN
import static io.github.lobodpav.spock.test.idea.ThreadingKt.invokeWriteAction

class SpockForKotlinTestCreatorExtensionSpec extends Specification {

    @WithIdea
    Idea idea

    def extension = new SpockForKotlinTestCreatorExtension()

    static def fileContent = """
        package foo.bar 
         
        const val rootLevelConstant = "A constant" 
         
        class TestClass {
            val testClassMessage = "Testing Kotlin class"
        
            fun testFunction(arg: String) {
                println(testClassMessage)
            }
        }
        
        object TestObject {
            val testObjectValue = "A value"
        }
        
        annotation class TestAnnotation {}
        """.stripIndent()

    def "The action is called for compatible elements"() {
        given: "The extension is created with mocked action (UI elements can't be tested)"
        def action = Mock(SpockForKotlinCreateTestAction)
        def extension = Spy(SpockForKotlinTestCreatorExtension) {
            it.action >> action
        }

        and: "A file is created"
        def psiFile = idea.loadFileContent("foo.bar.Test", fileContent, TestModule.ROOT, KOTLIN_MAIN)

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
        caretOffset                                            || expectedAvailability
        0                                                      || false
        fileContent.indexOf("const val rootLevelConstant")     || true
        fileContent.indexOf("TestClass {")                     || true
        fileContent.indexOf("testClassMessage =")              || true
        fileContent.indexOf("fun testFunction(arg")            || true
        fileContent.indexOf("TestObject {")                    || true
        fileContent.indexOf("val testObjectValue")             || true
        fileContent.indexOf("annotation class TestAnnotation") || true
        fileContent.length() - 1                               || false
    }

    def "Provides correct action"() {
        expect:
        extension.action instanceof SpockForKotlinCreateTestAction
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
