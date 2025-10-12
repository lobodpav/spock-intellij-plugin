rootProject.name = "spock-intellij-plugin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.2.20")

            plugin("kotlin", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("intellij", "org.jetbrains.intellij.platform").version("2.9.0")
            plugin("changelog", "org.jetbrains.changelog").version("2.4.0")

            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
        }

        create("testLibs") {
            version("groovy", "4.0.28")
            version("spock", "2.4-M6-groovy-4.0")

            library("groovy", "org.apache.groovy", "groovy").versionRef("groovy")
            // A workaround: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-faq.html#junit5-test-framework-refers-to-junit4
            library("junit4", "junit", "junit").version("4.13.2")
            // Allows mocking final classes
            library("mockito-core", "org.mockito", "mockito-core").version("5.20.0")
            // Allows mocking classes without a non-parametric constructor
            library("objenesis", "org.objenesis", "objenesis").version("3.4")
            library("spock-core", "org.spockframework", "spock-core").versionRef("spock")

            bundle("unit-spec", listOf("groovy", "objenesis", "spock-core"))
        }
    }
}
