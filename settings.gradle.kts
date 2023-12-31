rootProject.name = "spock-intellij-plugin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.9.21")

            library("kotlin-stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").versionRef("kotlin")
        }

        create("testLibs") {
            version("groovy", "4.0.13")
            version("spock", "2.3-groovy-4.0")

            library("groovy", "org.apache.groovy", "groovy").versionRef("groovy")
            // Allows mocking classes without a non-parametric constructor
            library("objenesis", "org.objenesis", "objenesis").version("3.3")
            library("spock-core", "org.spockframework", "spock-core").versionRef("spock")

            bundle("unit-spec", listOf("groovy", "objenesis", "spock-core"))
        }
    }
}
