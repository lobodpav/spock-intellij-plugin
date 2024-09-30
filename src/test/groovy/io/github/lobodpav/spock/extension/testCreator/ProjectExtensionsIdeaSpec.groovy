package io.github.lobodpav.spock.extension.testCreator

import io.github.lobodpav.spock.extension.testCreator.dialog.CreateSpecificationDialogOutput
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static io.github.lobodpav.spock.test.idea.SourceRoot.GROOVY_TEST
import static io.github.lobodpav.spock.test.idea.TestModule.MODULE1
import static io.github.lobodpav.spock.test.idea.TestModule.MODULE2
import static io.github.lobodpav.spock.test.idea.TestModule.ROOT
import static io.github.lobodpav.spock.test.idea.ThreadingKt.invokeWriteAction

class ProjectExtensionsIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    def "Gets modules for a Dialog module selector"() {
        when:
        def moduleSelectorItems = ProjectExtensionsKt.getModulesForSelector(idea.project)

        then:
        verifyAll(moduleSelectorItems) {
            it*.displayString == ["<project-root>", "module1", "module2"]
            it*.module*.name == ["test", "module1", "module2"]
            it*.groovyTestSourceRoot*.url == ["temp:///src/test/groovy", "temp:///module1/src/test/groovy", "temp:///module2/src/test/groovy"]
        }

        and:
        moduleSelectorItems*.toString() == ["<project-root>", "module1", "module2"]
    }

    def "Creates a Specification based on the user's input"() {
        given: "Assemble an output from the dialog window once the user hits the OK button"
        def dialogOutput = new CreateSpecificationDialogOutput(className, idea.getTestSourceRoot(module), destinationPackage, fqSuperClassName)
        def superClassName = fqSuperClassName.decomposeFqClassName().v2

        when: "The new Spec creation is initiated"
        invokeWriteAction { ProjectExtensionsKt.createSpecification(idea.project, dialogOutput) }

        and: "The created Spec file is loaded"
        def moduleDirectoryPrefix = module.sourceRootPrefix.empty ? "" : "${module.sourceRootPrefix}/"
        def virtualFilePath = "${moduleDirectoryPrefix}${GROOVY_TEST.path}/${destinationPackage.replace('.', '/')}/${className}.groovy"
        def virtualFile = idea.findVirtualFile(virtualFilePath)

        /*
            Note: The created file is opened in real IntelliJ editor, but cannot be verified in tests since the
                  `com.intellij.mock.Mock.MyFileEditorManager.openTextEditor` is mocked and does nothing.
         */
        then: "The Spec was created"
        virtualFile

        and: "The created Spec contains the new Spec class"
        verifyAll(virtualFile.inputStream.text) {
            it.startsWith("""
                package $destinationPackage

                import $fqSuperClassName

                class $className extends $superClassName {
                """.stripIndent().trim())
        }

        where:
        className      | module  | destinationPackage | fqSuperClassName
        "MyTestClass1" | ROOT    | "foo.bar.baz"      | "i.am.abstract.CustomSpec"
        "MyTestClass2" | MODULE1 | "test"             | "spock.lang.Specification"
        "MyTestClass3" | MODULE2 | "test"             | "Spec"
    }

    def "Creates a Specification without a root package specified"() {
        given: "Assemble an output from the dialog window once the user hits the OK button"
        def dialogOutput = new CreateSpecificationDialogOutput("ATest", idea.getTestSourceRoot(ROOT), "", "spock.lang.Specification")

        when: "The new Spec creation is initiated"
        invokeWriteAction { ProjectExtensionsKt.createSpecification(idea.project, dialogOutput) }

        and: "The created Spec file is loaded"
        def virtualFile = idea.findVirtualFile("${GROOVY_TEST.path}/ATest.groovy")


        /*
            Note: The created file is opened in real IntelliJ editor, but cannot be verified in tests since the
                  `com.intellij.mock.Mock.MyFileEditorManager.openTextEditor` is mocked and does nothing.
         */
        then: "The Spec was created"
        virtualFile

        and: "The created Spec contains the new Spec class without in the default package"
        verifyAll(virtualFile.inputStream.text) {
            !it.find("package")
            it.startsWith("""
                import spock.lang.Specification

                class ATest extends Specification {
                """.stripIndent().trim())
        }
    }
}
