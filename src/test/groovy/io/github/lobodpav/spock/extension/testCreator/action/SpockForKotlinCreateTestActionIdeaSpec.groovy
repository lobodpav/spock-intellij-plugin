package io.github.lobodpav.spock.extension.testCreator.action

import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.TestModule
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static io.github.lobodpav.spock.test.idea.SourceRoot.KOTLIN_MAIN
import static io.github.lobodpav.spock.test.idea.ThreadingKt.invokeWriteAction

class SpockForKotlinCreateTestActionIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    def action = new SpockForKotlinCreateTestAction()

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

    def "Gets class information for an element"() {
        given: "A file is created"
        def psiFile = idea.loadFileContent("foo.bar.Test", fileContent, TestModule.ROOT, KOTLIN_MAIN)

        and: "The caret is moved to an offset"
        invokeWriteAction { idea.editor.caretModel.moveToOffset(caretOffset) }

        when: "The element at caret is retrieved"
        def psiElement = psiFile.read { findElementAt(caretOffset) }

        and:
        def sourceClassInfo = action.readIt { it.getContainingClassOrNull(psiElement) }

        then:
        sourceClassInfo == expectedSourceClassInfo

        where:
        caretOffset                                            || expectedSourceClassInfo
        0                                                      || new SourceClassInfo("foo.bar", "TestKt", "foo.bar.TestKt")
        fileContent.indexOf("const val rootLevelConstant")     || new SourceClassInfo("foo.bar", "TestKt", "foo.bar.TestKt")
        fileContent.indexOf("TestClass {")                     || new SourceClassInfo("foo.bar", "TestClass", "foo.bar.TestClass")
        fileContent.indexOf("testClassMessage =")              || new SourceClassInfo("foo.bar", "TestClass", "foo.bar.TestClass")
        fileContent.indexOf("fun testFunction(arg")            || new SourceClassInfo("foo.bar", "TestClass", "foo.bar.TestClass")
        fileContent.indexOf("TestObject {")                    || new SourceClassInfo("foo.bar", "TestObject", "foo.bar.TestObject")
        fileContent.indexOf("val testObjectValue")             || new SourceClassInfo("foo.bar", "TestObject", "foo.bar.TestObject")
        fileContent.indexOf("annotation class TestAnnotation") || new SourceClassInfo("foo.bar", "TestAnnotation", "foo.bar.TestAnnotation")
        fileContent.length() - 1                               || new SourceClassInfo("foo.bar", "TestKt", "foo.bar.TestKt")
    }

    def "Gets class information when a package is not defined"() {
        given: "A file is created"
        def psiFile = idea.loadFileContent("foo.bar.TestClass", "class TestClass", TestModule.ROOT, KOTLIN_MAIN)

        when: "The element at 0 caret position is retrieved"
        def psiElement = psiFile.read { findElementAt(0) }

        and:
        def sourceClassInfo = action.readIt { it.getContainingClassOrNull(psiElement) }

        then: "The qualified name is the same as the class name"
        sourceClassInfo == new SourceClassInfo("", "TestClass", "TestClass")
    }
}
