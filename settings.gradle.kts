rootProject.name = "spock-intellij-plugin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.23")

            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
        }

        create("testLibs") {
            version("groovy", "4.0.20")
            version("spock", "2.4-M2-groovy-4.0")

            library("groovy", "org.apache.groovy", "groovy").versionRef("groovy")
            // Allows mocking final classes
            library("mockito-core", "org.mockito", "mockito-core").version("5.11.0")
            // Allows mocking classes without a non-parametric constructor
            library("objenesis", "org.objenesis", "objenesis").version("3.3")
            library("spock-core", "org.spockframework", "spock-core").versionRef("spock")

            bundle("unit-spec", listOf("groovy", "objenesis", "spock-core"))
        }
    }
}
