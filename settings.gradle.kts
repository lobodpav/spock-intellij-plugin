rootProject.name = "spock-intellij-plugin"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {

        }

        create("testLibs") {
            version("groovy", "4.0.13")
            version("spock", "2.3-groovy-4.0")

            library("groovy", "org.apache.groovy", "groovy").versionRef("groovy")
            library("spock-core", "org.spockframework", "spock-core").versionRef("spock")

            bundle("unit-spec", listOf("groovy", "spock-core"))
        }
    }
}
