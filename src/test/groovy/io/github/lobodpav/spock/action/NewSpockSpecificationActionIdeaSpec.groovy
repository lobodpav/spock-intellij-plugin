package io.github.lobodpav.spock.action

import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiJavaFile
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

class NewSpockSpecificationActionIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    def newSpockSpecificationAction = new NewSpockSpecificationAction()

    def "The action produces a groovy file with a Specification class"() {
        given:
        // TODO: Change the source folder structure - currently the temp:///src is marked as Java source root
        //       ModuleSourceRootData(module=Module: 'light_idea_test_case', customContentRoot=temp:///src, rootType=java-source, packagePrefix=, forGeneratedSources=false)
        //       The temp:///src dir is created here: com.intellij.testFramework.LightProjectDescriptor.setUpProject()
        //       FIND a way to create both source and test source dirs withing the module (src/main/kotlin and src/test/groovy)
        //          Maybe a policy? See LightJavaCodeInsightFixtureTestCase.getTempDirFixture()
        //              Find out how to set `idea.test.execution.policy` for a custom `IdeaTestExecutionPolicy`


        def sourceDirectory = idea.findOrCreateDirectory("my/cool/name")

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

        then: "There is a single class extension only"
        extendedClasses.size() == 1

        and: "The extended class is a Specification"
        extendedClasses[0].read { canonicalText } == "spock.lang.Specification"
    }

    def "The action is available in the test source directory only"() {
        // TODO check that the action.isAvailable() returns true only for `src/test/*`
        // TODO check that without spock on classpath the action is not available

        given:
//        def applicationManager = TestApplicationManager.getInstance()
//        applicationManager.dataProvider = new TestDataProvider(idea.project)

        def ideView = idea.createIdeView(sourceDirectory)
        def ideViewContext = SimpleDataContext.builder()
            .add(LangDataKeys.PROJECT, idea.project)
            .add(LangDataKeys.MODULE, idea.module)
            .add(LangDataKeys.IDE_VIEW, ideView)
            .build()

        when:
        def actionIsAvailable = newSpockSpecificationAction.read { isAvailable(ideViewContext) }

        then:
        actionIsAvailable == expectedAvailability

        where:
        sourceDirectory   || expectedAvailability
        "src/main/java"   || false
        "src/main/kotlin" || false
        "src/main/groovy" || false
        "src/test/java"   || true
        "src/test/kotlin" || true
        "src/test/groovy" || true
    }
}
