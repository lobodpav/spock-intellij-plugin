package io.github.lobodpav.spock.test.provider

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.impl.SimpleDataContext
import io.github.lobodpav.spock.test.idea.Idea
import io.github.lobodpav.spock.test.idea.TestModule

class DataContextProvider {

    static Map defaults() {
        [
            sourceDirectory: "src/test/groovy",
            testModule     : TestModule.ROOT,
        ]
    }

    static DataContext make(Idea idea, Map overrides = [:]) {
        def props = defaults() + overrides

        def ideView = IdeViewProvider.make(idea, [relativeDirectoryPath: props.sourceDirectory as String])

        return SimpleDataContext.builder()
            .add(LangDataKeys.PROJECT, idea.project)
            .add(LangDataKeys.MODULE, idea.getModule(props.testModule as TestModule))
            .add(LangDataKeys.IDE_VIEW, ideView)
            .build()
    }
}
