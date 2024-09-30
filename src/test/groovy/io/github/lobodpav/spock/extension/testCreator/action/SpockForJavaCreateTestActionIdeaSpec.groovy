package io.github.lobodpav.spock.extension.testCreator.action

import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.TestModule
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static io.github.lobodpav.spock.test.idea.SourceRoot.JAVA_MAIN
import static io.github.lobodpav.spock.test.idea.ThreadingKt.invokeWriteAction

class SpockForJavaCreateTestActionIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    def action = new SpockForJavaCreateTestAction()

    static def fileContent = """
        package foo.bar; 
         
        class Test {
            static final String message = "Testing Java class";
        
            void test(String arg) {
                System.out.println(message);
            }
        }
        
        @interface Annotation {}
        """.stripIndent()

    def "Gets class information for an element"() {
        given: "A file is created"
        def psiFile = idea.loadFileContent("foo.bar.Test", fileContent, TestModule.ROOT, JAVA_MAIN)

        and: "The caret is moved to an offset"
        invokeWriteAction { idea.editor.caretModel.moveToOffset(caretOffset) }

        when: "The element at caret is retrieved"
        def psiElement = psiFile.read { findElementAt(caretOffset) }

        and:
        def sourceClassInfo = action.readIt { it.getContainingClassOrNull(psiElement) }

        then:
        sourceClassInfo == expectedSourceClassInfo

        where:
        caretOffset                                  || expectedSourceClassInfo
        0                                            || null
        fileContent.indexOf("message =")             || new SourceClassInfo("foo.bar", "Test", "foo.bar.Test")
        fileContent.indexOf("Test {")                || new SourceClassInfo("foo.bar", "Test", "foo.bar.Test")
        fileContent.indexOf("test(String")           || new SourceClassInfo("foo.bar", "Test", "foo.bar.Test")
        fileContent.indexOf("@interface Annotation") || new SourceClassInfo("foo.bar", "Annotation", "foo.bar.Annotation")
        fileContent.length() - 1                     || null
    }

    def "Gets class information when a package is not defined"() {
        given: "A file is created"
        def psiFile = idea.loadFileContent("foo.bar.TestClass", "class TestClass {}", TestModule.ROOT, JAVA_MAIN)

        when: "The element at 0 caret position is retrieved"
        def psiElement = psiFile.read { findElementAt(0) }

        and:
        def sourceClassInfo = action.readIt { it.getContainingClassOrNull(psiElement) }

        then: "The qualified name is the same as the class name"
        sourceClassInfo == new SourceClassInfo("", "TestClass", "TestClass")
    }
}
