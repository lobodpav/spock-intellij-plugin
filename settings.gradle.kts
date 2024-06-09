rootProject.name = "spock-intellij-plugin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.0.0")

            plugin("kotlin", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("intellij", "org.jetbrains.intellij").version("1.17.2")
            plugin("changelog", "org.jetbrains.changelog").version("2.2.0")

            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
        }

        create("testLibs") {
            version("groovy", "4.0.21")
            version("spock", "2.4-M4-groovy-4.0")

            library("groovy", "org.apache.groovy", "groovy").versionRef("groovy")
            // Allows mocking final classes
            library("mockito-core", "org.mockito", "mockito-core").version("5.12.0")
            // Allows mocking classes without a non-parametric constructor
            library("objenesis", "org.objenesis", "objenesis").version("3.4")
            library("spock-core", "org.spockframework", "spock-core").versionRef("spock")

            bundle("unit-spec", listOf("groovy", "objenesis", "spock-core"))
        }
    }
}
