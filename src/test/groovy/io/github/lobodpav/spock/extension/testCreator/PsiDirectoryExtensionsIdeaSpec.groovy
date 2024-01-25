package io.github.lobodpav.spock.extension.testCreator

import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.WithIdea
import spock.lang.Specification

import static io.github.lobodpav.spock.test.idea.ThreadingKt.invokeWriteAction

class PsiDirectoryExtensionsIdeaSpec extends Specification {

    @WithIdea
    Idea idea

    def "Creates a package within a PsiDirectory"() {
        given:
        def sourceRootPsiDirectory = idea.findOrCreateDirectory("src/main/kotlin")

        when:
        invokeWriteAction { PsiDirectoryExtensionsKt.createPackage(sourceRootPsiDirectory, "a.b.c") }

        and:
        def createdPsiDirectory = idea.findDirectory("src/main/kotlin/a/b/c")

        then:
        createdPsiDirectory.virtualFile.getUrl() == "temp:///src/main/kotlin/a/b/c"
    }
}
