package io.github.lobodpav.spock.test.idea

enum class TestModule(val moduleName: String, val sourceRootPrefix: String) {
    /**
     * Gradle projects create `main` and `test` root modules.
     *
     * Maven multi-module projects do not have a root module.
     */
    ROOT("test", ""),
    MODULE1("module1", "module1"),
    MODULE2("module2", "module2"),
}
